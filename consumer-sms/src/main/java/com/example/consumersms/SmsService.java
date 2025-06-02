package com.example.consumersms;

import com.example.shared.Sms;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    public void sendSms(Sms sms) {
        try {
            // Simula l'invio dell'SMS tramite un provider SMS (es. Twilio, AWS SNS, ecc.)
            System.out.println("📱 SMS Consumer - Sending SMS:");
            System.out.println("   From: " + sms.getFrom());
            System.out.println("   To: " + sms.getTo());
            System.out.println("   Message: " + sms.getBody());
            System.out.println("   Message Length: " + (sms.getBody() != null ? sms.getBody().length() : 0) + " characters");
            
            // Simula il tempo di invio dell'SMS (più veloce delle email)
            Thread.sleep(300);
            
            System.out.println("✅ SMS sent successfully to " + sms.getTo());
            
        } catch (Exception e) {
            System.err.println("❌ Failed to send SMS: " + e.getMessage());
            throw new RuntimeException("SMS sending failed", e);
        }
    }
}
