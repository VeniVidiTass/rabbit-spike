package com.rabbit.bridge.mongorabbitbridge.service;

import com.example.shared.Email;
import com.rabbit.bridge.mongorabbitbridge.config.BridgeConfig;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import jakarta.annotation.PostConstruct;
import org.bson.Document;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.messaging.ChangeStreamRequest;
import org.springframework.data.mongodb.core.messaging.MessageListener;
import org.springframework.data.mongodb.core.messaging.Subscription;
import org.springframework.data.mongodb.core.messaging.DefaultMessageListenerContainer;
import org.springframework.data.mongodb.core.messaging.MessageListenerContainer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Date;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Component
public class MongoRabbitBridge {

    private static final String EMAIL_FROM = "no-reply@gestmed.com";

    private final MessageListenerContainer listenerContainer;
    private final RabbitTemplate rabbitTemplate;
    private final MongoTemplate mongoTemplate;
    private final BridgeConfig props;

    public MongoRabbitBridge(MongoTemplate mongoTemplate,
                             RabbitTemplate rabbitTemplate,
                             BridgeConfig props) {
        this.mongoTemplate = mongoTemplate;
        this.rabbitTemplate = rabbitTemplate;
        this.props = props;
        this.listenerContainer = new DefaultMessageListenerContainer(mongoTemplate);
    }

    @PostConstruct
    public void init() {
        listenerContainer.start();

        System.out.println("Monitoring Appointment Collection: " + props.getAppointmentCollection());
        System.out.println("Publishing to Email Queue: " + props.getEmailQueue());

        ChangeStreamRequest<Document> apptRequest = ChangeStreamRequest.builder(
                        (MessageListener<ChangeStreamDocument<Document>, Document>) msg -> {
                            Document d = msg.getBody();

                            // extract directly from Document
                            String patientEmail = d.getString("patient_email");
                            String patientName  = d.getString("patient_full_name");
                            String code         = d.getString("code");
                            Date when           = d.getDate("appointment_date");
                            Integer doctorId    = d.getInteger("doctor_id");

                            if (patientEmail == null || patientEmail.isEmpty()) {
                                throw new IllegalArgumentException("Patient email missing in appointment: " + d.toJson());
                            }

                            String subject = "Conferma Appuntamento " + code;
                            String body = String.format(
                                    "Ciao %s,%n%n" +
                                            "Il tuo appuntamento \"%s\" con il Dott. %d è fissato per il %s.%n" +
                                            "Codice prenotazione: %s.%n%n" +
                                            "Grazie!",
                                    patientName,
                                    code,
                                    doctorId,
                                    when,
                                    code
                            );

                            Email email = new Email(EMAIL_FROM, patientEmail, subject, body);
                            email.setScheduledAt(new Date());

                            System.out.println("Forwarding Appointment→Email: " + email);
                            rabbitTemplate.convertAndSend(props.getEmailQueue(), email);
                        })
                .collection(props.getAppointmentCollection())
                .filter(newAggregation(
                        match(where("operationType").is("insert"))
                ))
                .build();

        Subscription apptSub = listenerContainer.register(apptRequest, Document.class);

        try {
            apptSub.await(Duration.ofSeconds(5));
            System.out.println("Appointment listener active.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Interrupted while waiting for appointment subscription activation");
        }
    }
}
