package com.rabbit.bridge.mongorabbitbridge.listener;

import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.rabbit.bridge.mongorabbitbridge.config.BridgeProperties;
import com.rabbit.bridge.mongorabbitbridge.dto.AppointmentDto;
import com.rabbit.bridge.mongorabbitbridge.exception.MissingEmailException;
import com.rabbit.bridge.mongorabbitbridge.service.EmailSenderService;
import com.rabbit.bridge.mongorabbitbridge.service.SmsSenderService;
import jakarta.annotation.PostConstruct;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.messaging.*;
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
    private final EmailSenderService emailService;
    private final SmsSenderService smsService;

    public AppointmentChangeListener(MongoTemplate mongo,
                                     BridgeProperties props,
                                     EmailSenderService emailService,
                                     SmsSenderService smsService) {
        this.props         = props;
        this.emailService  = emailService;
        this.smsService    = smsService;
        this.listenerContainer = new DefaultMessageListenerContainer(mongo);
    }

    @PostConstruct
    public void start() {
        listenerContainer.register(
                ChangeStreamRequest.builder(this)
                        .collection(props.getAppointmentCollection())
                        .filter(newAggregation(match(where("operationType").is("insert"))))
                        .build(),
                Document.class
        );
        listenerContainer.start();
        log.info("AppointmentChangeListener active on '{}'", props.getAppointmentCollection());
    }

    @Override
    public void onMessage(Message<ChangeStreamDocument<Document>, Document> msg) {
        Document d = msg.getBody();
        log.debug("Raw event: {}", d.toJson());

        try {
            Set<String> excluded = Set.of(
                    "_id","patient_id","patient_full_name","patient_email",
                    "patient_codice_fiscale","patient_phone","doctor_id",
                    "service_id","code","appointment_date","created_at",
                    "updated_at","status"
            );
            Map<String, Object> extras = new HashMap<>();
            for (String k : d.keySet()) {
                if (!excluded.contains(k)) extras.put(k, d.get(k));
            }

            AppointmentDto dto = new AppointmentDto(
                    d.getString("patient_email"),
                    d.getString("patient_full_name"),
                    d.getString("patient_phone"),
                    d.getString("code"),
                    d.getDate("appointment_date"),
                    d.getInteger("doctor_id"),
                    d.getObjectId("service_id").toHexString(),
                    extras
            );

            emailService.sendAppointmentEmail(dto);
            smsService.sendAppointmentSms(dto);

        } catch (MissingEmailException me) {
            log.warn("No email for appointment – skipping", me);
        } catch (Exception e) {
            log.error("Error in change listener", e);
        }
    }
}
