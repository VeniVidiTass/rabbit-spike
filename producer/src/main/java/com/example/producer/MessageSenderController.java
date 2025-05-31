package com.example.producer;

import com.example.shared.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.Random;

@Controller
public class MessageSenderController {

    private final RabbitTemplate rabbitTemplate;
    private final MessageRecordRepository messageRepo;
    private final Random random = new Random();

    @Autowired
    public MessageSenderController(RabbitTemplate rabbitTemplate, MessageRecordRepository messageRepo) {
        this.rabbitTemplate = rabbitTemplate;
        this.messageRepo = messageRepo;
    }

    @GetMapping("/")
    public String showForm(Model model) {
        model.addAttribute("message", new Message());
        return "form";
    }

    @PostMapping("/send")
    public String sendMessage(@ModelAttribute Message message) {
        try {
            System.out.println("Received message - From: " + message.getFrom() + 
                             ", To: " + message.getTo() + 
                             ", Subject: " + message.getSubject() + 
                             ", Body: " + message.getBody());
            
            rabbitTemplate.convertAndSend(RabbitConfig.QUEUE_NAME, message);
            System.out.println("Message sent to RabbitMQ queue: " + RabbitConfig.QUEUE_NAME);
            
            saveToMongoAsync(message);
            return "redirect:/";
        } catch (Exception e) {
            System.err.println("Error in sendMessage: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/?error";
        }
    }@Async
    public void saveToMongoAsync(Message message) {
        try {
            // Current time + random 0–24 hours
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.HOUR, random.nextInt(25)); // 0–24 inclusive
            message.setScheduledAt(cal.getTime());
            String body = message.getBody();
            System.out.println("Saving message: " + (body != null ? body : "No body provided") + " at " + message.getScheduledAt().toString());
            
            // Actually save to MongoDB
            messageRepo.save(message);
            System.out.println("Message saved to MongoDB with ID: " + message.getId());
        } catch (Exception e) {
            System.err.println("Error saving message to MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
