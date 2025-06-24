package com.rabbit.bridge.mongorabbitbridge.service;

import com.rabbit.bridge.mongorabbitbridge.model.Appointment;
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
import java.util.Date;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Component
public class MongoRabbitBridge {

    private static final String EMAIL_FROM = "no-reply@gestmed.com";

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

    private Email buildAppointmentEmail(Appointment appt) {
        if (appt == null) {
            throw new IllegalArgumentException("Appointment cannot be null");
        }
        String to = appt.getPatientEmail();
        if (to == null || to.isEmpty()) {
            throw new IllegalArgumentException("Patient email is missing in appointment");
        }
        String subject = "Conferma Appuntamento " + (appt.getCode() != null ? appt.getCode() : "");
        String patientName = appt.getPatientFullName() != null ? appt.getPatientFullName() : "Paziente";
        String code = appt.getCode() != null ? appt.getCode() : "";
        String doctorId = appt.getDoctorId() != null ? appt.getDoctorId().toString() : "N/A";
        String date = appt.getAppointmentDate() != null ? appt.getAppointmentDate().toString() : "data non disponibile";
        String body = String.format(
                "Ciao %s,%n%n" +
                        "Il tuo appuntamento \"%s\" con il Dott. %s è fissato per il %s.%n" +
                        "Codice prenotazione: %s.%n%n" +
                        "Grazie!",
                patientName,
                code,
                doctorId,
                date,
                code
        );
        Email email = new Email(EMAIL_FROM, to, subject, body);
        email.setScheduledAt(new Date());
        return email;
    }

    @PostConstruct
    public void init() {
        listenerContainer.start();

        // LOGS: Print the collections & queues being monitored
        System.out.println("Monitoring MongoDB collections:");
        System.out.println(" - Appointment Collection:  " + props.getAppointmentCollection());
        System.out.println("RabbitMQ Queues:");
        System.out.println(" - Email Queue:             " + props.getEmailQueue());

        // ——— APPOINTMENTS subscription (emits into Email queue) ———
        ChangeStreamRequest<Document> apptRequest = ChangeStreamRequest.builder(
                        (MessageListener<ChangeStreamDocument<Document>, Document>) msg -> {
                            Document raw = msg.getBody();
                            System.out.println("Raw appointment document: " + raw.toJson());
                            Appointment appt = mongoTemplate.getConverter().read(Appointment.class, raw);
                            Email email = buildAppointmentEmail(appt);
                            System.out.println("Forwarding Appointment→Email: " + email);
                            rabbitTemplate.convertAndSend(props.getEmailQueue(), email);
                        })
                .collection(props.getAppointmentCollection())
                .filter(newAggregation(match(where("operationType").is("insert"))))
                .build();
        Subscription apptSub = listenerContainer.register(apptRequest, Document.class);
        // wait for all three to activate
        try {
            apptSub.await(Duration.ofSeconds(5));
            System.out.println("ChangeStream listeners active for 'email', 'sms' & 'appointments'.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Interrupted while waiting for subscriptions activation");
        }
    }
}
