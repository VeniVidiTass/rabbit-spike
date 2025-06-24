package com.rabbit.bridge.mongorabbitbridge.client;

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
    private final Duration timeout = Duration.ofSeconds(3);

    public DoctorApiClient(
            @Value("${app.bridge.service-api-base-url}") String baseUrl
    ) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public DoctorDto getDoctorById(Integer doctorId) {
        String idStr = doctorId.toString();
        return webClient.get()
                .uri("/doctors/{id}", idStr)
                .exchangeToMono(resp -> {
                    if (resp.statusCode().is2xxSuccessful()) {
                        return resp.bodyToMono(DoctorDto.class);
                    } else {
                        log.debug("Non-2xx {} fetching doctor '{}'; falling back",
                                resp.statusCode().value(), idStr);
                        return Mono.just(fallback(idStr));
                    }
                })
                .timeout(timeout)
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
        return d;
    }

    /** Minimal subset of your Doctor payload */
    public static class DoctorDto {
        private String id;
        private String name;
        public String getId()               { return id;   }
        public void setId(String id)       { this.id = id; }
        public String getName()            { return name; }
        public void setName(String name)   { this.name = name; }
    }
}
