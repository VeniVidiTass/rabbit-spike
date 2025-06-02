package com.example.consumeremail;

import com.example.shared.Email;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(Email email) throws RuntimeException {
        try {
            SimpleMailMessage emailMessage = new SimpleMailMessage();
            emailMessage.setFrom(email.getFrom());
            emailMessage.setTo(email.getTo());
            emailMessage.setSubject(email.getSubject());
            emailMessage.setText(email.getBody());

            mailSender.send(emailMessage);
            System.out.println("📧 Email sent successfully to " + email.getTo());
        } catch (Exception e) {
            System.err.println("❌ Failed to send email: " + e.getMessage());
            throw new RuntimeException("Email sending failed", e);
        }
    }
}
