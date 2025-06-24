package com.rabbit.bridge.mongorabbitbridge.service;

import com.example.shared.Email;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbit.bridge.mongorabbitbridge.config.BridgeConfig;
import com.rabbit.bridge.mongorabbitbridge.dto.ServiceDto;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import jakarta.annotation.PostConstruct;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.messaging.ChangeStreamRequest;
import org.springframework.data.mongodb.core.messaging.DefaultMessageListenerContainer;
import org.springframework.data.mongodb.core.messaging.MessageListener;
import org.springframework.data.mongodb.core.messaging.MessageListenerContainer;
import org.springframework.data.mongodb.core.messaging.Subscription;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Component
public class MongoRabbitBridge {

    private static final String EMAIL_FROM = "no-reply@gestmed.com";

    private final MessageListenerContainer listenerContainer;
    private final RabbitTemplate rabbitTemplate;
    private final BridgeConfig props;
    private final RestTemplate restTemplate;

    public MongoRabbitBridge(MongoTemplate mongoTemplate,
                             RabbitTemplate rabbitTemplate,
                             BridgeConfig props,
                             RestTemplate restTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.props = props;
        this.restTemplate = restTemplate;
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
                            System.out.println("Received Appointment Change: " + d.toJson());

                            // Extract fields
                            String patientEmail = d.getString("patient_email");
                            String patientName  = d.getString("patient_full_name");
                            String code         = d.getString("code");
                            Date when           = d.getDate("appointment_date");
                            Integer doctorId    = d.getInteger("doctor_id");
                            ObjectId service_id = d.getObjectId("service_id");

                            // Build additionalInfo map
                            Set<String> excludedKeys = Set.of(
                                    "patient_email", "patient_full_name", "code", "appointment_date",
                                    "doctor_id", "service_id", "notes", "_id", "created_at", "updated_at"
                            );
                            Map<String, String> additionalInfo = d.keySet().stream()
                                    .filter(key -> !excludedKeys.contains(key))
                                    .collect(Collectors.toMap(key -> key, key -> String.valueOf(d.get(key))));

                            // 1) Fetch service details (raw)
                            String sid = service_id.toHexString();
                            String url = props.getServiceApiBaseUrl()
                                    + "/appointments/services/" + sid;

                            try {
                                // Get full response as a String
                                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                                String rawJson = response.getBody();
                                System.out.println("Raw service response for ID " + sid + ": " + rawJson);

                                // If you still want to map it afterward, you can do:
                                ObjectMapper mapper = new ObjectMapper();
                                ServiceDto service = mapper.readValue(rawJson, ServiceDto.class);
                                additionalInfo.put("service_name", service.getName());
                                additionalInfo.put("service_description", service.getDescription());

                            } catch (RestClientException | JsonProcessingException e) {
                                System.err.println("Error fetching/parsing service " + sid + ": " + e.getMessage());
                                additionalInfo.put("service_fetch_error", e.getMessage());
                            }


                            // 2) Build & send email
                            String subject = "Conferma Appuntamento " + code;
                            String body = String.format(
                                    "Ciao %s,%n%n" +
                                            "Il tuo appuntamento \"%s\" con il Dott. %d è fissato per il %s.%n" +
                                            "Codice prenotazione: %s.%n" +
                                            "Informazioni aggiuntive: %s.%n%n" +
                                            "Grazie!",
                                    patientName, code, doctorId, when, code, additionalInfo
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
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
