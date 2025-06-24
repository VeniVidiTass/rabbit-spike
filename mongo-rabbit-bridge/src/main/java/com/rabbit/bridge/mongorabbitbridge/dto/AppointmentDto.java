package com.rabbit.bridge.mongorabbitbridge.dto;

import java.util.Date;
import java.util.Map;

/**
 * Data we care about from each Mongo appointment,
 * including phone for SMS notifications.
 */
public class AppointmentDto {

    private final String patientEmail;
    private final String patientName;
    private final String patientPhone;   
    private final String code;
    private final Date   appointmentDate;
    private final Integer doctorId;
    private final String  serviceId;
    private final Map<String, Object> extraFields;

    public AppointmentDto(String patientEmail,
                          String patientName,
                          String patientPhone,    
                          String code,
                          Date appointmentDate,
                          Integer doctorId,
                          String serviceId,
                          Map<String, Object> extraFields) {
        this.patientEmail    = patientEmail;
        this.patientName     = patientName;
        this.patientPhone    = patientPhone;  
        this.code            = code;
        this.appointmentDate = appointmentDate;
        this.doctorId        = doctorId;
        this.serviceId       = serviceId;
        this.extraFields     = extraFields;
    }

    public String getPatientEmail()       { return patientEmail; }
    public String getPatientName()        { return patientName; }
    public String getPatientPhone()       { return patientPhone; } 
    public String getCode()               { return code; }
    public Date   getAppointmentDate()    { return appointmentDate; }
    public Integer getDoctorId()          { return doctorId; }
    public String getServiceId()          { return serviceId; }
    public Map<String, Object> getExtraFields() { return extraFields; }

    @Override
    public String toString() {
        return "AppointmentDto{" +
                "patientEmail='"   + patientEmail   + '\'' +
                ", patientName='"  + patientName    + '\'' +
                ", patientPhone='" + patientPhone   + '\'' +
                ", code='"         + code           + '\'' +
                ", appointmentDate="+ appointmentDate+
                ", doctorId="      + doctorId       +
                ", serviceId='"    + serviceId      + '\'' +
                ", extraFields="   + extraFields    +
                '}';
    }
}
