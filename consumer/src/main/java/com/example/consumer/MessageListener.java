package com.example.consumer;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class MessageListener {

    private final Random random = new Random();

    @RabbitListener(queues = RabbitConfig.QUEUE_NAME)
    public void receiveMessage(Message message, Channel channel) throws Exception {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String body = new String(message.getBody());

        try {
            // Simulate random processing delay (0–3 seconds)
            int delayMillis = random.nextInt(3001); // 0 to 3000 ms
            System.out.println("📨 Received message: " + body + " (processing for " + delayMillis + "ms)");
            Thread.sleep(delayMillis);

            // 33% chance of simulated failure
            if (random.nextInt(3) == 0) {
                throw new RuntimeException("Simulated processing failure");
            }

            // If successful
            System.out.println("✅ Processed message: " + body);
            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage() + " — NACKing and requeuing...");
            channel.basicNack(deliveryTag, false, true); // Requeue = true
        }
    }
}
