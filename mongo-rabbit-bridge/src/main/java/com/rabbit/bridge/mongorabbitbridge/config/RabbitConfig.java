package com.rabbit.bridge.mongorabbitbridge.config;

import com.example.shared.Email;
import com.example.shared.Sms;
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

    /**
     * Configure a Jackson-based message converter that knows how to
     * (de)serialize our shared Email and Sms classes.
     */
    @Bean
    public MessageConverter producerJackson2MessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();

        DefaultClassMapper classMapper = new DefaultClassMapper();
        // Trust only our shared package
        classMapper.setTrustedPackages("com.example.shared");
        // Map incoming type IDs to actual classes
        classMapper.setIdClassMapping(Map.of(
                "com.example.shared.Email", Email.class,
                "com.example.shared.Sms",   Sms.class
        ));

        converter.setClassMapper(classMapper);
        return converter;
    }

    /**
     * RabbitTemplate that uses our JSON message converter.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            MessageConverter producerJackson2MessageConverter
    ) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(producerJackson2MessageConverter);
        return template;
    }
}
