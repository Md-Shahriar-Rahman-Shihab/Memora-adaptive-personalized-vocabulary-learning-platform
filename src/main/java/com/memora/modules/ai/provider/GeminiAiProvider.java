package com.memora.modules.ai.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.memora.modules.ai.config.AiConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Google Gemini implementation of {@link AiProvider}.
 * Calls the Generative Language REST API with controlled timeouts,
 * graceful exception handling, and automatic delegation to {@link FallbackAiProvider}.
 */
@Component("geminiAiProvider")
public class GeminiAiProvider implements AiProvider {

    private static final Logger log = LoggerFactory.getLogger(GeminiAiProvider.class);
    private static final String GEMINI_API_URL_TEMPLATE = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";

    private final AiConfig aiConfig;
    private final RestClient restClient;
    private final FallbackAiProvider fallbackAiProvider;
    private final ObjectMapper objectMapper;

    public GeminiAiProvider(
            AiConfig aiConfig,
            @Qualifier("aiRestClient") RestClient restClient,
            FallbackAiProvider fallbackAiProvider,
            ObjectMapper objectMapper) {
        this.aiConfig = aiConfig;
        this.restClient = restClient;
        this.fallbackAiProvider = fallbackAiProvider;
        this.objectMapper = objectMapper;
    }

    @Override
    public AiGenerationResult generate(String prompt) {
        if (!isAvailable()) {
            log.info("[AI-FLOW] provider=fallback model=null success=true reason=not_configured");
            return fallbackAiProvider.generate(prompt);
        }

        String primaryModel = aiConfig.getModel();
        log.info("[GEMINI-PROVIDER] Executing request for primary model: {}", primaryModel);
        String result = executeModelRequest(primaryModel, prompt);
        if (result != null && !result.isBlank()) {
            log.info("[AI-FLOW] provider=gemini model={} success=true", primaryModel);
            return AiGenerationResult.success(result, "gemini", primaryModel);
        }

        // Resilient model upgrade: if primary model was retired or rate-limited (e.g. 404/429)
        List<String> fallbackModels = List.of("gemini-3.7-flash", "gemini-3.5-flash");
        for (String fallbackModel : fallbackModels) {
            if (!fallbackModel.equalsIgnoreCase(primaryModel)) {
                log.info("[GEMINI-PROVIDER] Model [{}] failed. Attempting active supported model [{}]...", primaryModel, fallbackModel);
                result = executeModelRequest(fallbackModel, prompt);
                if (result != null && !result.isBlank()) {
                    log.info("[AI-FLOW] provider=gemini model={} success=true fallbackFrom={}", fallbackModel, primaryModel);
                    return AiGenerationResult.success(result, "gemini", fallbackModel);
                }
            }
        }

        log.warn("[AI-FLOW] provider=fallback model=null success=true reason=all_models_failed");
        return fallbackAiProvider.generate(prompt);
    }

    @Override
    public String generateContent(String prompt) {
        return generate(prompt).text();
    }

    private String executeModelRequest(String model, String prompt) {
        try {
            String url = String.format(GEMINI_API_URL_TEMPLATE, model, aiConfig.getApiKey());

            Map<String, Object> requestPayload = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", prompt)
                            ))
                    ),
                    "generationConfig", Map.of(
                            "temperature", aiConfig.getTemperature(),
                            "maxOutputTokens", 400
                    )
            );

            String responseBody = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON, MediaType.ALL)
                    .body(requestPayload)
                    .exchange((clientRequest, clientResponse) -> {
                        if (!clientResponse.getStatusCode().is2xxSuccessful()) {
                            log.warn("[AI-FLOW] provider=gemini model={} success=false reason={}", model, clientResponse.getStatusCode().value());
                            try {
                                InputStream stream = clientResponse.getBody();
                                byte[] bytes = stream != null ? stream.readAllBytes() : new byte[0];
                                String err = new String(bytes, StandardCharsets.UTF_8).replaceAll("key=[^&\\s]+", "key=REDACTED");
                                log.warn("Gemini API error status [{}] for model [{}]: {}",
                                        clientResponse.getStatusCode(), model, err);
                            } catch (Exception streamEx) {
                                log.warn("Gemini API error status [{}] for model [{}]",
                                        clientResponse.getStatusCode(), model);
                            }
                            return null;
                        }
                        InputStream stream = clientResponse.getBody();
                        byte[] bytes = stream != null ? stream.readAllBytes() : new byte[0];
                        return new String(bytes, StandardCharsets.UTF_8);
                    });

            if (responseBody == null || responseBody.isBlank()) {
                log.warn("Empty response received from Gemini API for model [{}].", model);
                return null;
            }

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                JsonNode parts = candidates.get(0).path("content").path("parts");
                if (parts.isArray() && !parts.isEmpty()) {
                    String generatedText = parts.get(0).path("text").asText("");
                    if (!generatedText.isBlank()) {
                        return cleanResponseText(generatedText);
                    }
                }
            }

            log.warn("Gemini response missing candidate parts for model [{}].", model);
            return null;

        } catch (Exception e) {
            String sanitizedError = e.getMessage() != null
                    ? e.getMessage().replaceAll("key=[^&\\s]+", "key=REDACTED")
                    : "Unknown error";
            log.warn("Gemini request for model [{}] failed: {}", model, sanitizedError);
            return null;
        }
    }

    @Override
    public String getProviderName() {
        return isAvailable() ? "gemini" : "fallback";
    }

    @Override
    public boolean isAvailable() {
        return aiConfig.isGeminiConfigured();
    }

    private String cleanResponseText(String text) {
        if (text == null) return "";
        // Strip unnecessary surrounding quotes or leading/trailing formatting if present
        String trimmed = text.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() > 2) {
            trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
        }
        return trimmed;
    }
}
