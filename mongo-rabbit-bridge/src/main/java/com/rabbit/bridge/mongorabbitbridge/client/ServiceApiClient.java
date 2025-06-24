package com.rabbit.bridge.mongorabbitbridge.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * A simple client to fetch service details by ID.
 * Logs at DEBUG on non-2xx responses, and falls back to using the raw ID as name.
 */
@Component
public class ServiceApiClient {

    private static final Logger log = LoggerFactory.getLogger(ServiceApiClient.class);
    private final WebClient webClient;
    private final Duration timeout = Duration.ofSeconds(3);

    public ServiceApiClient(
            @Value("${app.bridge.service-api-base-url}") String baseUrl
    ) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public ServiceDto getServiceById(String serviceId) {
        return webClient.get()
                .uri("/appointments/services/{id}", serviceId)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(ServiceDto.class);
                    } else {
                        log.debug("Non-2xx status {} when fetching service '{}'; falling back to ID as name",
                                response.statusCode().value(), serviceId);
                        ServiceDto fallback = new ServiceDto();
                        fallback.setId(serviceId);
                        fallback.setName(serviceId);
                        return Mono.just(fallback);
                    }
                })
                .timeout(timeout)
                .onErrorResume(ex -> {
                    log.warn("Failed to fetch service '{}' ({}); using ID as name",
                            serviceId, ex.toString());
                    ServiceDto fallback = new ServiceDto();
                    fallback.setId(serviceId);
                    fallback.setName(serviceId);
                    return Mono.just(fallback);
                })
                .block();
    }

    public static class ServiceDto {
        private String id;
        private String name;

        public String getId()   { return id; }
        public void setId(String id)   { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}
