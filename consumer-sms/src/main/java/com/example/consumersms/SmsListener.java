package com.example.consumersms;

import com.example.shared.Sms;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class SmsListener {

    private final Random random = new Random();
    private final SmsService smsService;

    public SmsListener(SmsService smsService) {
        this.smsService = smsService;
    }

    @RabbitListener(queues = RabbitConfig.SMS_QUEUE_NAME)
    public void receiveSms(Sms message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws Exception {
        String msg = message.toString();

        try {
            // Simula ritardo casuale di elaborazione (0–1.5 secondi, più veloce delle email)
            int delayMillis = random.nextInt(1501); // 0 to 1500 ms
            System.out.println("📱 SMS Consumer - Received SMS message: " + msg + " (processing for " + delayMillis + "ms)");
            Thread.sleep(delayMillis);

            // 15% di possibilità di fallimento simulato (meno degli email per essere più affidabile)
            if (random.nextInt(7) == 0) {
                throw new RuntimeException("Simulated SMS processing failure");
            }

            // Invia SMS tramite il servizio SMS
            smsService.sendSms(message);

            // Se l'invio ha successo
            System.out.println("✅ SMS Consumer - Processed SMS message successfully: " + msg);
            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            System.err.println("❌ SMS Consumer - Error: " + e.getMessage() + " — NACKing and requeuing...");
            channel.basicNack(deliveryTag, false, true); // Requeue = true
        }
    }
}
