package com.example.consumer;

import com.example.shared.Message;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class MessageListener {

    private final Random random = new Random();
    private final EmailService emailService;
    public MessageListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_NAME)
    public void receiveMessage(Message message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws Exception {
        String msg = message.toString();

        try {
            // Simulate random processing delay (0–3 seconds)
            int delayMillis = random.nextInt(3001); // 0 to 3000 ms
            System.out.println("📨 Received message: " + msg + " (processing for " + delayMillis + "ms)");
            Thread.sleep(delayMillis);

            // 33% chance of simulated failure
            if (random.nextInt(3) == 0) {
                throw new RuntimeException("Simulated processing failure");
            }

            // Send email via SMTP to MailDev
            emailService.sendEmail(message);

            // If successful
            System.out.println("✅ Processed message: " + msg);
            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            // BUG: if the email has syntax errors, the requeuing will cause an infinite loop
            System.err.println("❌ Error: " + e.getMessage() + " — NACKing and requeuing...");
            channel.basicNack(deliveryTag, false, true); // Requeue = true
        }
    }
}
