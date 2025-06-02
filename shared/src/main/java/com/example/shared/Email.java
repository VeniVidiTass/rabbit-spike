package com.example.shared;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

// passare ad usare direttamente org.springframework.mail.SimpleMailMessage
@Document(collection = "email")
public class Email implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    private String from;
    private String to;
    private String subject;
    private String body;
    private Date scheduledAt = null;

    public Email() {}

    public Email(String from, String to, String subject, String body) {
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.body = body;
    }

    public void setScheduledAt() {
        this.scheduledAt = new Date();
    }

    public void setScheduledAt(Date scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public String getId() { return id; }
    
    public void setId(String id) { this.id = id; }
    
    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }
    
    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }
    
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
      public Date getScheduledAt() { return scheduledAt; }
    
    // For form binding (th:field="*{text}" maps to body)
    public String getText() { return body; }
    public void setText(String text) { this.body = text; }

    @Override
    public String toString() {
        return "Email{" +
                "from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", subject='" + subject + '\'' +
                ", body='" + body + '\'' +
                '}';
    }
}
