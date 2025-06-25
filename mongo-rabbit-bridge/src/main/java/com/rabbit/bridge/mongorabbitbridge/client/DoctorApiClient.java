package com.rabbit.bridge.mongorabbitbridge.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Fetches doctor details by ID.
 * On any error or non-2xx, falls back to using the raw ID as the name.
 */
@Component
public class DoctorApiClient {

    private static final Logger log = LoggerFactory.getLogger(DoctorApiClient.class);
    private final WebClient webClient;
    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    public DoctorApiClient(@Value("${app.bridge.service-api-base-url}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public DoctorDto getDoctorById(Integer doctorId) {
        String idStr = doctorId.toString();
        return webClient.get()
                .uri("/doctors/{id}", idStr)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(DoctorDto.class);
                    } else {
                        log.debug("Non-2xx status {} when fetching doctor '{}'; falling back",
                                response.statusCode().value(), idStr);
                        return Mono.just(fallback(idStr));
                    }
                })
                .timeout(TIMEOUT)
                .onErrorResume(ex -> {
                    log.warn("Error fetching doctor '{}': {}; falling back",
                            idStr, ex.toString());
                    return Mono.just(fallback(idStr));
                })
                .block();
    }

    private DoctorDto fallback(String id) {
        DoctorDto d = new DoctorDto();
        d.setId(id);
        d.setName(id);
        d.setLicenseNumber("license_number");
        return d;
    }

    /** Minimal subset of your Doctor payload */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DoctorDto {
        private String id;
        private String name;
        @JsonProperty("license_number")
        private String licenseNumber;

        public String getId()                 { return id; }
        public void setId(String id)          { this.id = id; }
        public String getName()               { return name; }
        public void setName(String name)      { this.name = name; }
        public String getLicenseNumber()      { return licenseNumber; }
        public void setLicenseNumber(String l){ this.licenseNumber = l; }
    }
}
