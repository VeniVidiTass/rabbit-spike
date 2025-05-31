package com.example.consumer;

import com.example.shared.Message;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(Message message) throws RuntimeException {
        try {
            SimpleMailMessage email = new SimpleMailMessage();
            email.setFrom(message.getFrom());
            email.setTo(message.getTo());
            email.setSubject(message.getSubject());
            email.setText(message.getBody());

            mailSender.send(email);
            System.out.println("📧 Email sent successfully to " + message.getTo());
        } catch (Exception e) {
            System.err.println("❌ Failed to send email: " + e.getMessage());
            throw new RuntimeException("Email sending failed", e);
        }
    }
}
