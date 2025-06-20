package com.rabbit.bridge.mongorabbitbridge.config;

import com.rabbit.bridge.mongorabbitbridge.service.MongoRabbitBridge;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class BridgeConfig {

    @Bean
    public MongoRabbitBridge bridge(MongoTemplate mongoTemplate, RabbitTemplate rabbitTemplate) {
        return new MongoRabbitBridge(mongoTemplate, rabbitTemplate);
    }
}
