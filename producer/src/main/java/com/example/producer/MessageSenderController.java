package com.example.producer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class MessageSenderController {

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public MessageSenderController(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @GetMapping("/")
    public String showForm(Model model) {
        model.addAttribute("message", new Message());
        return "form";
    }

    @PostMapping("/send")
    public String sendMessage(@ModelAttribute Message message) {
        rabbitTemplate.convertAndSend(RabbitConfig.QUEUE_NAME, message.getText());
        return "redirect:/";
    }

    // Simple wrapper class for the form input
    public static class Message {
        private String text;

        public String getText() { return text; }

        public void setText(String text) { this.text = text; }
    }
}
