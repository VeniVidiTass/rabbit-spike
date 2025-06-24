package com.rabbit.bridge.mongorabbitbridge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.bridge")
public class BridgeConfig {
    private String emailCollection;
    private String smsCollection;

    // - GETTERS -
    public String getEmailCollection() {
        return emailCollection;
    }
    public String getSmsCollection() {
        return smsCollection;
    }

    // - SETTERS -
    public void setEmailCollection(String emailCollection) {
        this.emailCollection = emailCollection;
    }
    public void setSmsCollection(String smsCollection) {
        this.smsCollection = smsCollection;
    }

}
