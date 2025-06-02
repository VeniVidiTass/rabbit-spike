package com.example.consumersms;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitConfig {

    public static final String SMS_QUEUE_NAME = "sms-queue";

    @Bean
    public Queue smsQueue() {
        return new Queue(SMS_QUEUE_NAME, false);
    }

    @Bean
    public MessageConverter messageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultClassMapper classMapper = new DefaultClassMapper();
        
        // Configura le classi trusted per la deserializzazione
        Map<String, Class<?>> idClassMapping = new HashMap<>();
        idClassMapping.put("com.example.shared.Sms", com.example.shared.Sms.class);
        classMapper.setIdClassMapping(idClassMapping);
        
        // Abilita la mappatura basata su tipo per sicurezza
        classMapper.setTrustedPackages("com.example.shared.*");
        
        converter.setClassMapper(classMapper);
        return converter;
    }
}
