package com.rabbit.bridge.mongorabbitbridge;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MongoRabbitBridgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(MongoRabbitBridgeApplication.class, args);
    }

    @Bean
    public ApplicationRunner blockForever() {
        return args -> {
            Thread.currentThread().join();
        };
    }
}
