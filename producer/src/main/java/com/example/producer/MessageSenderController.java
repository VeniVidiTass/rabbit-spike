package com.example.producer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.Date;
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
        rabbitTemplate.convertAndSend(RabbitConfig.QUEUE_NAME, message.getText());
        saveToMongoAsync(message.getText());
        return "redirect:/";
    }

    @Async
    public void saveToMongoAsync(String text) {
        // Current time + random 0–24 hours
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, random.nextInt(25)); // 0–24 inclusive

        MessageRecord record = new MessageRecord(text, cal.getTime());
        messageRepo.save(record);
        System.out.println("💾 Saved to MongoDB: " + text + " at " + record.getScheduledAt());
    }

    public static class Message {
        private String text;
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }
}
