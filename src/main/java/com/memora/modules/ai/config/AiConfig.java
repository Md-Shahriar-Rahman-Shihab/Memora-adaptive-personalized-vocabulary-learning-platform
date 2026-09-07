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

    @Value("${memora.ai.provider:${MEMORA_AI_PROVIDER:auto}}")
    private String provider;

    @Value("${memora.ai.gemini.api-key:${MEMORA_GEMINI_API_KEY:${memora.ai.api-key:${MEMORA_AI_API_KEY:}}}}")
    private String apiKey;

    @Value("${memora.ai.gemini.model:${MEMORA_GEMINI_MODEL:${memora.ai.model:${MEMORA_AI_MODEL:gemini-3.6-flash}}}}")
    private String model;

    @Value("${memora.ai.groq.api-key:${MEMORA_GROQ_API_KEY:}}")
    private String groqApiKey;

    @Value("${memora.ai.groq.model:${MEMORA_GROQ_MODEL:openai/gpt-oss-120b}}")
    private String groqModel;

    @Value("${memora.ai.timeout-ms:${MEMORA_AI_TIMEOUT_MS:6000}}")
    private int timeoutMs;

    @Value("${memora.ai.temperature:${MEMORA_AI_TEMPERATURE:0.3}}")
    private double temperature;

    @PostConstruct
    public void logDiagnostics() {
        log.info("AI Provider: {}", getProvider());
        log.info("Gemini Model: {}", getModel());
        log.info("Gemini API Key: {}", getApiKey().isBlank() ? "NOT CONFIGURED" : "CONFIGURED");
        log.info("Groq Model: {}", getGroqModel());
        log.info("Groq API Key: {}", getGroqApiKey().isBlank() ? "NOT CONFIGURED" : "CONFIGURED");
    }

    public String getProvider() {
        return provider != null && !provider.isBlank() ? provider.trim().toLowerCase() : "auto";
    }

    public String getApiKey() {
        return apiKey != null ? apiKey.trim() : "";
    }

    public String getModel() {
        return model != null && !model.isBlank() ? model.trim() : "gemini-3.6-flash";
    }

    public String getGroqApiKey() {
        return groqApiKey != null ? groqApiKey.trim() : "";
    }

    public String getGroqModel() {
        return groqModel != null && !groqModel.isBlank() ? groqModel.trim() : "openai/gpt-oss-120b";
    }

    public int getTimeoutMs() {
        return timeoutMs > 0 ? timeoutMs : 6000;
    }

    public double getTemperature() {
        return temperature;
    }

    public boolean isGeminiConfigured() {
        return ("gemini".equalsIgnoreCase(getProvider()) || "auto".equalsIgnoreCase(getProvider()))
                && !getApiKey().isBlank();
    }

    public boolean isGroqConfigured() {
        return ("groq".equalsIgnoreCase(getProvider()) || "auto".equalsIgnoreCase(getProvider()))
                && !getGroqApiKey().isBlank();
    }

    public boolean isAiConfigured() {
        String p = getProvider();
        if ("gemini".equalsIgnoreCase(p)) {
            return isGeminiConfigured();
        }
        if ("groq".equalsIgnoreCase(p)) {
            return isGroqConfigured();
        }
        if ("auto".equalsIgnoreCase(p)) {
            return isGeminiConfigured() || isGroqConfigured();
        }
        return false;
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
