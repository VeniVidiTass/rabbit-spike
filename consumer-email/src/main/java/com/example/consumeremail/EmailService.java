package com.example.consumeremail;

import com.example.shared.Email;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(Email email) {
        try {
            // 1) create a MimeMessage
            MimeMessage msg = mailSender.createMimeMessage();

            // 2) use the helper, set 'true' for HTML
            MimeMessageHelper helper = new MimeMessageHelper(msg, false, "UTF-8");
            helper.setFrom(email.getFrom());
            helper.setTo(email.getTo());
            helper.setSubject(email.getSubject());
            helper.setText(email.getBody(), true); // <-- true = isHtml

            // 3) send
            mailSender.send(msg);
            System.out.println("📧 Email sent successfully to " + email.getTo());

        } catch (MessagingException e) {
            System.err.println("❌ Failed to build HTML email: " + e.getMessage());
            throw new RuntimeException("Email building failed", e);
        } catch (Exception e) {
            System.err.println("❌ Failed to send email: " + e.getMessage());
            throw new RuntimeException("Email sending failed", e);
        }
    }
}
