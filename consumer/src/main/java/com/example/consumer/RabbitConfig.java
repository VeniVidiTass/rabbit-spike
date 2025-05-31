package com.example.consumer;

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

    public static final String QUEUE_NAME = "simple-queue";

    @Bean
    public Queue queue() {
        return new Queue(QUEUE_NAME, false);
    }

    @Bean
    public MessageConverter messageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultClassMapper classMapper = new DefaultClassMapper();
        
        // Configura le classi trusted per la deserializzazione
        Map<String, Class<?>> idClassMapping = new HashMap<>();
        idClassMapping.put("com.example.shared.Message", com.example.shared.Message.class);
        classMapper.setIdClassMapping(idClassMapping);
        
        // Abilita la mappatura basata su tipo per sicurezza
        classMapper.setTrustedPackages("com.example.shared.*");
        
        converter.setClassMapper(classMapper);
        return converter;
    }
}
