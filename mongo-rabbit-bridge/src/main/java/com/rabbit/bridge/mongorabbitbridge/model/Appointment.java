package com.rabbit.bridge.mongorabbitbridge.model;

import org.bson.types.ObjectId;
import org.bson.codecs.pojo.annotations.BsonExtraElements;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;

public class Appointment {
    private Integer patientId;
    private String patientFullName;
    private String patientEmail;
    private String patientCodiceFiscale;
    private String patientPhone;
    private Integer doctorId;
    private ObjectId serviceId;
    private String code;
    private Date appointmentDate;
    private String status;
    private String notes;
    private Date createdAt;
    private Date updatedAt;

    @BsonExtraElements
    private Map<String, Object> extraElements = new HashMap<>();

    public Appointment() {
    }

    public Integer getPatientId() {
        return patientId;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    public String getPatientFullName() {
        return patientFullName;
    }

    public void setPatientFullName(String patientFullName) {
        this.patientFullName = patientFullName;
    }

    public String getPatientEmail() {
        return patientEmail;
    }

    public void setPatientEmail(String patientEmail) {
        this.patientEmail = patientEmail;
    }

    public String getPatientCodiceFiscale() {
        return patientCodiceFiscale;
    }

    public void setPatientCodiceFiscale(String patientCodiceFiscale) {
        this.patientCodiceFiscale = patientCodiceFiscale;
    }

    public String getPatientPhone() {
        return patientPhone;
    }

    public void setPatientPhone(String patientPhone) {
        this.patientPhone = patientPhone;
    }

    public Integer getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Integer doctorId) {
        this.doctorId = doctorId;
    }

    public ObjectId getServiceId() {
        return serviceId;
    }

    public void setServiceId(ObjectId serviceId) {
        this.serviceId = serviceId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Date getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(Date appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Map<String, Object> getExtraElements() {
        return extraElements;
    }

    public void setExtraElements(Map<String, Object> extraElements) {
        this.extraElements = extraElements;
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "patientId=" + patientId +
                ", patientFullName='" + patientFullName + '\'' +
                ", patientEmail='" + patientEmail + '\'' +
                ", doctorId=" + doctorId +
                ", serviceId=" + serviceId +
                ", code='" + code + '\'' +
                ", extraElements=" + extraElements +
                '}';
    }
}
