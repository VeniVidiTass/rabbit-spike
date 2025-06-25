package com.rabbit.bridge.mongorabbitbridge.mapper;

import com.example.shared.Email;
import com.rabbit.bridge.mongorabbitbridge.dto.AppointmentDto;
import com.rabbit.bridge.mongorabbitbridge.exception.MissingEmailException;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Date;

@Component
public class AppointmentToEmailMapper {

    private static final String FROM_ADDRESS = "no-reply@gestmed.com";
    private final SpringTemplateEngine templateEngine;

    public AppointmentToEmailMapper(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    /**
     * Render the Thymeleaf template into an HTML string.
     */
    public Email map(AppointmentDto appt,
                     String serviceName,
                     int durationMinutes,
                     String doctorName,
                     String licenseNumber) {
        if (appt.getPatientEmail() == null || appt.getPatientEmail().isBlank()) {
            throw new MissingEmailException("Patient email missing for: " + appt);
        }

        // 1) prepare the Thymeleaf context
        Context ctx = new Context();
        ctx.setVariable("patientName",     appt.getPatientName());
        ctx.setVariable("code",            appt.getCode());
        ctx.setVariable("serviceName",     serviceName);
        ctx.setVariable("durationMinutes", durationMinutes);
        ctx.setVariable("doctorName",      doctorName);
        ctx.setVariable("licenseNumber",   licenseNumber);
        ctx.setVariable("appointmentDate", appt.getAppointmentDate());
        ctx.setVariable("extras",          appt.getExtraFields());

        // 2) process the template
        String htmlBody = templateEngine.process(
                "appointment-confirmation",  // src/main/resources/templates/appointment-confirmation.html
                ctx
        );

        // 3) build the Email object
        String subject = String.format(
                "Conferma Appuntamento %s – %s",
                appt.getCode(), serviceName
        );
        Email email = new Email(
                FROM_ADDRESS,
                appt.getPatientEmail(),
                subject,
                htmlBody
        );
        email.setScheduledAt(new Date());
        return email;
    }
}
