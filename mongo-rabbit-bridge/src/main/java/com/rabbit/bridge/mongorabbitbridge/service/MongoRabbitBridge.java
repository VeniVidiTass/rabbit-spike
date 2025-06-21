package com.rabbit.bridge.mongorabbitbridge.service;

import com.example.shared.Email;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.rabbit.bridge.mongorabbitbridge.config.RabbitConfig;
import jakarta.annotation.PostConstruct;
import org.bson.Document;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
 * Bridge service that listens to MongoDB Change Streams and forwards inserts to RabbitMQ as typed Emails.
 */
@Component
public class MongoRabbitBridge {

    private final MessageListenerContainer listenerContainer;
    private final RabbitTemplate rabbitTemplate;
    private final MongoTemplate mongoTemplate;

    public MongoRabbitBridge(MongoTemplate mongoTemplate,
                             RabbitTemplate rabbitTemplate) {
        this.mongoTemplate = mongoTemplate;
        this.rabbitTemplate = rabbitTemplate;
        this.listenerContainer = new DefaultMessageListenerContainer(mongoTemplate);
    }

    @PostConstruct
    public void init() {
        listenerContainer.start();

        ChangeStreamRequest<Document> request = ChangeStreamRequest.builder(
                        (MessageListener<ChangeStreamDocument<Document>, Document>) msg -> {
                            Document raw = msg.getBody();
                            // map to your shared Email
                            Email email = mongoTemplate.getConverter()
                                    .read(Email.class, raw);
                            System.out.println("Forwarding Email: " + email);
                            // send the Email object; Jackson converter will handle JSON + headers
                            rabbitTemplate.convertAndSend(
                                    RabbitConfig.EMAIL_QUEUE,
                                    email
                            );
                        })
                .collection("email")
                .filter(newAggregation(
                        match(where("operationType").is("insert"))
                ))
                .build();

        Subscription sub = listenerContainer.register(request, Document.class);

        try {
            sub.await(Duration.ofSeconds(5));
            System.out.println("ChangeStream listener is now active for collection 'email'.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Interrupted while waiting for ChangeStream subscription activation");
        }
    }
}
