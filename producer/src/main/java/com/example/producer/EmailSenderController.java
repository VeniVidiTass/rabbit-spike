package com.example.producer;

import com.example.shared.Email;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.Random;

@Controller
public class EmailSenderController {

    private final RabbitTemplate rabbitTemplate;
    private final EmailRecordRepository emailRepo;
    private final Random random = new Random();

    @Autowired
    public EmailSenderController(RabbitTemplate rabbitTemplate, EmailRecordRepository emailRepo) {
        this.rabbitTemplate = rabbitTemplate;
        this.emailRepo = emailRepo;
    }

    @GetMapping("/")
    public String showForm(Model model) {
        model.addAttribute("email", new Email());
        return "form";
    }

    @PostMapping("/send")
    public String sendEmail(@ModelAttribute Email email) {
        try {
            System.out.println("Sending email - From: " + email.getFrom() +
                    ", To: " + email.getTo() +
                    ", Subject: " + email.getSubject() +
                    ", Body: " + email.getBody());

            rabbitTemplate.convertAndSend(RabbitConfig.EMAIL_QUEUE_NAME, email);
            System.out.println("Email sent to RabbitMQ queue: " + RabbitConfig.EMAIL_QUEUE_NAME);

            saveToMongoAsync(email);
            return "redirect:/";
        } catch (Exception e) {
            System.err.println("Error in sendEmail: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/?error";
        }
    }

    @Async
    public void saveToMongoAsync(Email email) {
        try {
            // Current time + random 0–24 hours
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.HOUR, random.nextInt(25)); // 0–24 inclusive
            email.setScheduledAt(cal.getTime());
            String body = email.getBody();
            System.out.println("Saving email: " + (body != null ? body : "No body provided") + " at "
                    + email.getScheduledAt().toString());

            // Actually save to MongoDB
            emailRepo.save(email);
            System.out.println("Email saved to MongoDB with ID: " + email.getId());
        } catch (Exception e) {
            System.err.println("Error saving email to MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
