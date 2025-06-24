package com.rabbit.bridge.mongorabbitbridge.listener;

import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.rabbit.bridge.mongorabbitbridge.config.BridgeProperties;
import com.rabbit.bridge.mongorabbitbridge.dto.AppointmentDto;
import com.rabbit.bridge.mongorabbitbridge.exception.MissingEmailException;
import com.rabbit.bridge.mongorabbitbridge.service.EmailSenderService;
import jakarta.annotation.PostConstruct;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.messaging.ChangeStreamRequest;
import org.springframework.data.mongodb.core.messaging.DefaultMessageListenerContainer;
import org.springframework.data.mongodb.core.messaging.Message;
import org.springframework.data.mongodb.core.messaging.MessageListener;
import org.springframework.data.mongodb.core.messaging.MessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Component
public class AppointmentChangeListener
        implements MessageListener<ChangeStreamDocument<Document>, Document> {

    private static final Logger log = LoggerFactory.getLogger(AppointmentChangeListener.class);

    private final MessageListenerContainer listenerContainer;
    private final BridgeProperties props;
    private final EmailSenderService emailSender;

    public AppointmentChangeListener(MongoTemplate mongoTemplate,
                                     BridgeProperties props,
                                     EmailSenderService emailSender) {
        this.props = props;
        this.emailSender = emailSender;
        this.listenerContainer = new DefaultMessageListenerContainer(mongoTemplate);
    }

    @PostConstruct
    public void start() {
        log.info("Starting listener for '{}'", props.getAppointmentCollection());

        ChangeStreamRequest<Document> request = ChangeStreamRequest.builder(this)
                .collection(props.getAppointmentCollection())
                .filter(newAggregation(match(where("operationType").is("insert"))))
                .build();

        listenerContainer.register(request, Document.class);
        listenerContainer.start();
        log.info("AppointmentChangeListener is now active.");
    }

    @Override
    public void onMessage(Message<ChangeStreamDocument<Document>, Document> message) {
        Document d = message.getBody();
        log.debug("Raw change event document: {}", d.toJson());

        try {
            // define all keys we do NOT want in 'extras'
            Set<String> known = Set.of(
                    "_id",
                    "patient_id",
                    "patient_full_name",
                    "patient_email",
                    "patient_codice_fiscale",
                    "patient_phone",
                    "doctor_id",
                    "service_id",
                    "code",
                    "appointment_date",
                    "created_at",
                    "updated_at"
            );

            Map<String, Object> extras = new HashMap<>();
            for (String key : d.keySet()) {
                if (!known.contains(key)) {
                    extras.put(key, d.get(key));
                }
            }

            String serviceId = d.getObjectId("service_id").toHexString();
            AppointmentDto dto = new AppointmentDto(
                    d.getString("patient_email"),
                    d.getString("patient_full_name"),
                    d.getString("code"),
                    d.getDate("appointment_date"),
                    d.getInteger("doctor_id"),
                    serviceId,
                    extras
            );

            emailSender.sendAppointmentEmail(dto);
            log.info("Forwarded appointment to email: {}", dto);

        } catch (MissingEmailException mee) {
            log.warn("Skipping appointment without email: {}", mee.getMessage());
        } catch (Exception e) {
            log.error("Error processing appointment change", e);
        }
    }
}
