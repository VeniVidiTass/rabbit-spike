package com.rabbit.bridge.mongorabbitbridge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.bridge")
public class BridgeConfig {

    private String emailQueue;
    private String smsQueue;
    private String appointmentCollection;
    private String serviceApiBaseUrl;

    // getters
    public String getEmailQueue()            { return emailQueue; }
    public String getSmsQueue()              { return smsQueue; }
    public String getAppointmentCollection() { return appointmentCollection; }
    public String getServiceApiBaseUrl()     { return serviceApiBaseUrl; }

    // setters
    public void setEmailQueue(String q)            { this.emailQueue = q; }
    public void setSmsQueue(String q)              { this.smsQueue = q; }
    public void setAppointmentCollection(String c) { this.appointmentCollection = c; }
    public void setServiceApiBaseUrl(String url)   { this.serviceApiBaseUrl = url; }
}
