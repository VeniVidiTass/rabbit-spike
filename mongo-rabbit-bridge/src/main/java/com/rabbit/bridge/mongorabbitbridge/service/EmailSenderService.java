package com.rabbit.bridge.mongorabbitbridge.service;

import com.example.shared.Email;
import com.rabbit.bridge.mongorabbitbridge.client.DoctorApiClient;
import com.rabbit.bridge.mongorabbitbridge.client.ServiceApiClient;
import com.rabbit.bridge.mongorabbitbridge.config.BridgeProperties;
import com.rabbit.bridge.mongorabbitbridge.dto.AppointmentDto;
import com.rabbit.bridge.mongorabbitbridge.mapper.AppointmentToEmailMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class EmailSenderService {

    private static final Logger log = LoggerFactory.getLogger(EmailSenderService.class);

    private final RabbitTemplate rabbit;
    private final BridgeProperties props;
    private final AppointmentToEmailMapper mapper;
    private final ServiceApiClient serviceClient;
    private final DoctorApiClient doctorClient;

    public EmailSenderService(RabbitTemplate rabbit,
                              BridgeProperties props,
                              AppointmentToEmailMapper mapper,
                              ServiceApiClient serviceClient,
                              DoctorApiClient doctorClient) {
        this.rabbit         = rabbit;
        this.props          = props;
        this.mapper         = mapper;
        this.serviceClient  = serviceClient;
        this.doctorClient   = doctorClient;
    }

    /**
     * Resolve service + doctor names, render HTML email, and enqueue.
     */
    public void sendAppointmentEmail(AppointmentDto dto) {
        String serviceName = serviceClient.getServiceById(dto.getServiceId()).getName();
        String doctorName  = doctorClient.getDoctorById(dto.getDoctorId()).getName();

        Email email = mapper.map(dto, serviceName, doctorName);
        rabbit.convertAndSend(props.getEmailQueue(), email);
        log.debug("Email enqueued → queue='{}', to='{}'",
                props.getEmailQueue(), dto.getPatientEmail());
    }
}
