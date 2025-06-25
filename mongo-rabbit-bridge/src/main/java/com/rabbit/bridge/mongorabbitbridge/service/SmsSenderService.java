// src/main/java/com/rabbit/bridge/mongorabbitbridge/service/SmsSenderService.java
package com.rabbit.bridge.mongorabbitbridge.service;

import com.example.shared.Sms;
import com.rabbit.bridge.mongorabbitbridge.client.DoctorApiClient;
import com.rabbit.bridge.mongorabbitbridge.client.ServiceApiClient;
import com.rabbit.bridge.mongorabbitbridge.config.BridgeProperties;
import com.rabbit.bridge.mongorabbitbridge.dto.AppointmentDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;

@Service
public class SmsSenderService {

    private static final Logger log = LoggerFactory.getLogger(SmsSenderService.class);
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private final RabbitTemplate    rabbit;
    private final BridgeProperties  props;
    private final ServiceApiClient  serviceClient;
    private final DoctorApiClient   doctorClient;

    public SmsSenderService(RabbitTemplate rabbit,
                            BridgeProperties props,
                            ServiceApiClient serviceClient,
                            DoctorApiClient doctorClient) {
        this.rabbit        = rabbit;
        this.props         = props;
        this.serviceClient = serviceClient;
        this.doctorClient  = doctorClient;
    }

    /**
     * If a phone number is present, resolve details and enqueue a plain-text SMS.
     */
    public void sendAppointmentSms(AppointmentDto dto) {
        String phone = dto.getPatientPhone();
        if (phone == null || phone.isBlank()) return;

        var svc = serviceClient.getServiceById(dto.getServiceId());
        var doc = doctorClient.getDoctorById(dto.getDoctorId());
        String when = DATE_FMT.format(dto.getAppointmentDate());

        String text = String.format(
                "GestMed: Appuntamento %s (Durata %d min) il %s con Dott. %s (%s). Codice %s.",
                svc.getName(),
                svc.getDurationMinutes(),
                when,
                doc.getName(),
                doc.getLicenseNumber(),
                dto.getCode()
        );

        Sms sms = new Sms("GestMed", phone, text);
        rabbit.convertAndSend(props.getSmsQueue(), sms);
        log.debug("SMS enqueued → queue='{}', to='{}': {}",
                props.getSmsQueue(), phone, text);
    }
}
