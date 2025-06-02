package com.example.producer;

import com.example.shared.Sms;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.Random;

@Controller
@RequestMapping("/sms")
public class SmsSenderController {

    private final RabbitTemplate rabbitTemplate;
    private final SmsRecordRepository smsRepo;
    private final Random random = new Random();

    @Autowired
    public SmsSenderController(RabbitTemplate rabbitTemplate, SmsRecordRepository smsRepo) {
        this.rabbitTemplate = rabbitTemplate;
        this.smsRepo = smsRepo;
    }

    @GetMapping("/")
    public String showSmsForm(Model model) {
        model.addAttribute("sms", new Sms());
        return "sms-form";
    }

    @PostMapping("/send")
    public String sendSms(@ModelAttribute Sms sms) {
        try {
            System.out.println("Sending SMS - From: " + sms.getFrom() +
                    ", To: " + sms.getTo() +
                    ", Body: " + sms.getBody());

            rabbitTemplate.convertAndSend(RabbitConfig.SMS_QUEUE_NAME, sms);
            System.out.println("SMS sent to RabbitMQ queue: " + RabbitConfig.SMS_QUEUE_NAME);

            saveToMongoAsync(sms);
            return "redirect:/sms/";
        } catch (Exception e) {
            System.err.println("Error in sendSms: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/sms/?error";
        }
    }

    @Async
    public void saveToMongoAsync(Sms sms) {
        try {
            // Current time + random 0–24 hours
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.HOUR, random.nextInt(25)); // 0–24 inclusive
            sms.setScheduledAt(cal.getTime());
            String body = sms.getBody();
            System.out.println("Saving SMS: " + (body != null ? body : "No body provided") + " at "
                    + sms.getScheduledAt().toString());

            // Actually save to MongoDB
            smsRepo.save(sms);
            System.out.println("SMS saved to MongoDB with ID: " + sms.getId());
        } catch (Exception e) {
            System.err.println("Error saving SMS to MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
