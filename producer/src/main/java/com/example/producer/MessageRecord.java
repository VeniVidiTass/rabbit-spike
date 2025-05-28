package com.example.producer;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "messages")
public class MessageRecord {

    @Id
    private String id;

    private String text;
    private Date scheduledAt;

    public MessageRecord() {}

    public MessageRecord(String text, Date scheduledAt) {
        this.text = text;
        this.scheduledAt = scheduledAt;
    }

    public String getId() { return id; }
    public String getText() { return text; }
    public Date getScheduledAt() { return scheduledAt; }
}
