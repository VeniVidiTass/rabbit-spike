package com.rabbit.bridge.mongorabbitbridge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.bridge")
public class BridgeProperties {

    /**
     * Name of the RabbitMQ queue for email messages.
     */
    private String emailQueue;

    /**
     * Name of the RabbitMQ queue for SMS messages.
     */
    private String smsQueue;

    /**
     * Name of the MongoDB collection to watch for appointments.
     */
    private String appointmentCollection;

    public String getEmailQueue() {
        return emailQueue;
    }

    public void setEmailQueue(String emailQueue) {
        this.emailQueue = emailQueue;
    }

    public String getSmsQueue() {
        return smsQueue;
    }

    public void setSmsQueue(String smsQueue) {
        this.smsQueue = smsQueue;
    }

    public String getAppointmentCollection() {
        return appointmentCollection;
    }

    public void setAppointmentCollection(String appointmentCollection) {
        this.appointmentCollection = appointmentCollection;
    }
}
