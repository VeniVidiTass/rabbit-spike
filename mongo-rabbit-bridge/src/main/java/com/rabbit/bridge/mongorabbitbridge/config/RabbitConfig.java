package com.rabbit.bridge.mongorabbitbridge.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class RabbitConfig {

    public static final String EMAIL_QUEUE = "email-queue";
    public static final String SMS_QUEUE   = "sms-queue";

    @Bean
    public Queue emailQueue() {
        return new Queue(EMAIL_QUEUE, false, false, false);
    }

    @Bean
    public Queue smsQueue() {
        return new Queue(SMS_QUEUE, false, false, false);
    }

    @Bean
    public MessageConverter producerJackson2MessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();

        DefaultClassMapper classMapper = new DefaultClassMapper();
        classMapper.setTrustedPackages("com.example.shared");
        classMapper.setIdClassMapping(Map.of(
                "com.example.shared.Email", com.example.shared.Email.class,
                "com.example.shared.Sms",   com.example.shared.Sms.class
        ));
        converter.setClassMapper(classMapper);

        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory cf,
            MessageConverter producerJackson2MessageConverter
    ) {
        RabbitTemplate rt = new RabbitTemplate(cf);
        rt.setMessageConverter(producerJackson2MessageConverter);
        return rt;
    }
}
