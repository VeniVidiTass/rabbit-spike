package com.rabbit.bridge.mongorabbitbridge.service;

import com.example.shared.Email;
import com.example.shared.Sms;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.rabbit.bridge.mongorabbitbridge.config.BridgeConfig;
import com.rabbit.bridge.mongorabbitbridge.config.RabbitConfig;
import jakarta.annotation.PostConstruct;
import org.bson.Document;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.messaging.*;
import org.springframework.stereotype.Component;

import java.time.Duration;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Component
public class MongoRabbitBridge {

    private final MessageListenerContainer listenerContainer;
    private final RabbitTemplate        rabbitTemplate;
    private final MongoTemplate         mongoTemplate;
    private final BridgeConfig props;

    public MongoRabbitBridge(MongoTemplate mongoTemplate,
                             RabbitTemplate rabbitTemplate,
                             BridgeConfig props) {
        this.props = props;
        this.mongoTemplate = mongoTemplate;
        this.rabbitTemplate = rabbitTemplate;
        this.listenerContainer = new DefaultMessageListenerContainer(mongoTemplate);
    }

    @PostConstruct
    public void init() {
        listenerContainer.start();

        // LOGS: Print the collections being monitored
        System.out.println("Monitoring MongoDB collections:");
        System.out.println(" - Email Collection: " + props.getEmailCollection());
        System.out.println(" - SMS Collection: " + props.getSmsCollection());

        // ——— EMAIL subscription ———
        ChangeStreamRequest<Document> emailRequest = ChangeStreamRequest.builder(
                        (MessageListener<ChangeStreamDocument<Document>, Document>) msg -> {
                            Document raw = msg.getBody();
                            Email email = mongoTemplate.getConverter().read(Email.class, raw);
                            System.out.println("Forwarding Email: " + email);
                            rabbitTemplate.convertAndSend(
                                    RabbitConfig.EMAIL_QUEUE,
                                    email
                            );
                        })
                .collection(props.getEmailCollection())
                .filter(newAggregation(
                        match(where("operationType").is("insert"))
                ))
                .build();

        Subscription emailSub = listenerContainer.register(emailRequest, Document.class);

        // ——— SMS subscription ———
        ChangeStreamRequest<Document> smsRequest = ChangeStreamRequest.builder(
                        (MessageListener<ChangeStreamDocument<Document>, Document>) msg -> {
                            Document raw = msg.getBody();
                            Sms sms = mongoTemplate.getConverter().read(Sms.class, raw);
                            System.out.println("Forwarding SMS: " + sms);
                            rabbitTemplate.convertAndSend(
                                    RabbitConfig.SMS_QUEUE,
                                    sms
                            );
                        })
                .collection(props.getSmsCollection())
                .filter(newAggregation(
                        match(where("operationType").is("insert"))
                ))
                .build();

        Subscription smsSub = listenerContainer.register(smsRequest, Document.class);

        // wait for both to activate
        try {
            emailSub.await(Duration.ofSeconds(5));
            smsSub.await(Duration.ofSeconds(5));
            System.out.println("ChangeStream listeners active for 'email' & 'sms'.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Interrupted while waiting for subscriptions activation");
        }
    }
}
