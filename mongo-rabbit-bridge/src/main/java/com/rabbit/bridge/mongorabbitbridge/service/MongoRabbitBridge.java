package com.rabbit.bridge.mongorabbitbridge.service;

import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.rabbit.bridge.mongorabbitbridge.config.RabbitConfig;
import jakarta.annotation.PostConstruct;
import org.bson.Document;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.messaging.ChangeStreamRequest;
import org.springframework.data.mongodb.core.messaging.DefaultMessageListenerContainer;
import org.springframework.data.mongodb.core.messaging.MessageListener;
import org.springframework.data.mongodb.core.messaging.MessageListenerContainer;
import org.springframework.data.mongodb.core.messaging.Subscription;
import org.springframework.stereotype.Component;

import java.time.Duration;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * Bridge service that listens to MongoDB Change Streams and forwards inserts to RabbitMQ.
 */
@Component
public class MongoRabbitBridge {

    private final MessageListenerContainer listenerContainer;
    private final RabbitTemplate rabbitTemplate;

    public MongoRabbitBridge(MongoTemplate mongoTemplate, RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.listenerContainer = new DefaultMessageListenerContainer(mongoTemplate);
    }

    @PostConstruct
    public void init() {
        listenerContainer.start();

        ChangeStreamRequest<Document> request = ChangeStreamRequest.builder((MessageListener<ChangeStreamDocument<Document>, Document>) msg -> {
                    Document body = msg.getBody();
                    System.out.println("New document inserted: " + body.toJson());
                    rabbitTemplate.convertAndSend(RabbitConfig.QUEUE_NAME, body.toJson());
                })
                .collection("email")
                .filter(newAggregation(match(where("operationType").is("insert"))))
                .build();

        Subscription subscription = listenerContainer.register(request, Document.class);
        try {
            subscription.await(Duration.ofSeconds(5));
            System.out.println("ChangeStream listener is now active for collection 'email'.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Interrupted while waiting for ChangeStream subscription activation");
        }
    }
}