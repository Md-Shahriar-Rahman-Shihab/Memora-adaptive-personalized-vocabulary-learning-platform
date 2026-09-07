package com.memora.modules.ai.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Configuration for Memora AI module, managing external LLM provider parameters,
 * controlled timeouts, and RestClient instances.
 */
@Configuration
public class AiConfig {

    private static final Logger log = LoggerFactory.getLogger(AiConfig.class);

    @Value("${memora.ai.provider:fallback}")
    private String provider;

    @Value("${memora.ai.api-key:}")
    private String apiKey;

    @Value("${memora.ai.model:gemini-3.6-flash}")
    private String model;

    @Value("${memora.ai.timeout-ms:6000}")
    private int timeoutMs;

    @Value("${memora.ai.temperature:0.3}")
    private double temperature;

    @PostConstruct
    public void logDiagnostics() {
        log.info("AI Provider: {}", getProvider());
        log.info("AI Model: {}", getModel());
        log.info("AI API Key: {}", getApiKey().isBlank() ? "NOT CONFIGURED" : "CONFIGURED");
    }

    public String getProvider() {
        return provider != null ? provider.trim().toLowerCase() : "fallback";
    }

    public String getApiKey() {
        return apiKey != null ? apiKey.trim() : "";
    }

    public String getModel() {
        return model != null && !model.isBlank() ? model.trim() : "gemini-3.6-flash";
    }

    public int getTimeoutMs() {
        return timeoutMs > 0 ? timeoutMs : 6000;
    }

    public double getTemperature() {
        return temperature;
    }

    public boolean isGeminiConfigured() {
        return "gemini".equalsIgnoreCase(getProvider()) && !getApiKey().isBlank();
    }

    @Bean(name = "aiRestClient")
    public RestClient aiRestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofMillis(timeoutMs).toMillis());
        factory.setReadTimeout((int) Duration.ofMillis(timeoutMs).toMillis());

        return RestClient.builder()
                .requestFactory(factory)
                .build();
    }
}
