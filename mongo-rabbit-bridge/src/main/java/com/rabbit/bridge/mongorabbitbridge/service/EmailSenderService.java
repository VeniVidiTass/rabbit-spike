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

    private final RabbitTemplate rabbitTemplate;
    private final BridgeProperties props;
    private final AppointmentToEmailMapper mapper;
    private final ServiceApiClient serviceClient;
    private final DoctorApiClient  doctorClient;

    public EmailSenderService(RabbitTemplate rabbitTemplate,
                              BridgeProperties props,
                              AppointmentToEmailMapper mapper,
                              ServiceApiClient serviceClient,
                              DoctorApiClient doctorClient
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.props          = props;
        this.mapper         = mapper;
        this.serviceClient  = serviceClient;
        this.doctorClient   = doctorClient;
    }

    public void sendAppointmentEmail(AppointmentDto dto) {
        // 1) lookup names
        String serviceName = serviceClient.getServiceById(dto.getServiceId()).getName();
        String doctorName  = doctorClient.getDoctorById(dto.getDoctorId()).getName();

        // 2) build & send
        Email email = mapper.map(dto, serviceName, doctorName);
        rabbitTemplate.convertAndSend(props.getEmailQueue(), email);

        log.debug("Sent appointment email (svc='{}', doc='{}') to '{}'",
                serviceName, doctorName, props.getEmailQueue());
    }
}
