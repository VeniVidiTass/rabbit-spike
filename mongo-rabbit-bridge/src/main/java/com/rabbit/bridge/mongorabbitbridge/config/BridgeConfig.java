package com.rabbit.bridge.mongorabbitbridge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.bridge")
public class BridgeConfig {

    private String emailCollection;
    private String smsCollection;

    private String emailQueue;
    private String smsQueue;

    public String appointmentCollection;

    // - GETTERS -
    public String getEmailCollection() {
        return emailCollection;
    }
    public String getSmsCollection() {
        return smsCollection;
    }
    public String getEmailQueue() {
        return emailQueue;
    }
    public String getSmsQueue() {
        return smsQueue;
    }
    public String getAppointmentCollection() {
        return appointmentCollection;
    }

    // - SETTERS -
    public void setEmailCollection(String emailCollection) {
        this.emailCollection = emailCollection;
    }
    public void setSmsCollection(String smsCollection) {
        this.smsCollection = smsCollection;
    }
    public void setEmailQueue(String emailQueue) {
        this.emailQueue = emailQueue;
    }
    public void setSmsQueue(String smsQueue) {
        this.smsQueue = smsQueue;
    }
    public void setAppointmentCollection(String appointmentCollection) {
        this.appointmentCollection = appointmentCollection;
    }

}
