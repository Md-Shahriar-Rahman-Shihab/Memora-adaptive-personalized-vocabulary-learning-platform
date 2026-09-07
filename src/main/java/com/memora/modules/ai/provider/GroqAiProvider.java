package com.memora.modules.ai.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.memora.modules.ai.config.AiConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Groq implementation of {@link AiProvider} utilizing Groq's high-speed,
 * OpenAI-compatible chat completions endpoint with the specified model (e.g., openai/gpt-oss-120b).
 * Supports single-attempt execution for safe multi-provider routing and graceful fallback.
 */
@Component("groqAiProvider")
public class GroqAiProvider implements AiProvider {

    private static final Logger log = LoggerFactory.getLogger(GroqAiProvider.class);
    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";

    private final AiConfig aiConfig;
    private final RestClient restClient;
    private final FallbackAiProvider fallbackAiProvider;
    private final ObjectMapper objectMapper;

    public GroqAiProvider(
            AiConfig aiConfig,
            @Qualifier("aiRestClient") RestClient restClient,
            FallbackAiProvider fallbackAiProvider,
            ObjectMapper objectMapper) {
        this.aiConfig = aiConfig;
        this.restClient = restClient;
        this.fallbackAiProvider = fallbackAiProvider;
        this.objectMapper = objectMapper;
    }

    /**
     * Executes a single attempt against the configured Groq model without retry loops.
     * Returns null on failure, 429 rate limit, or timeout.
     *
     * @param prompt The prompt to execute
     * @return Result if successful, or null on failure
     */
    public AiGenerationResult generateDirect(String prompt) {
        if (!isAvailable()) {
            return null;
        }

        String model = aiConfig.getGroqModel();
        log.info("[GROQ-PROVIDER] Executing request for model: {}", model);
        String result = executeGroqRequest(model, prompt);
        if (result != null && !result.isBlank()) {
            log.info("[AI-FLOW] provider=groq model={} success=true", model);
            return AiGenerationResult.success(result, "groq", model);
        }
        return null;
    }

    @Override
    public AiGenerationResult generate(String prompt) {
        AiGenerationResult directResult = generateDirect(prompt);
        if (directResult != null) {
            return directResult;
        }
        log.warn("[AI-FLOW] provider=fallback model=null success=true reason=groq_failed");
        return fallbackAiProvider.generate(prompt);
    }

    @Override
    public String generateContent(String prompt) {
        return generate(prompt).text();
    }

    private String executeGroqRequest(String model, String prompt) {
        try {
            Map<String, Object> requestPayload = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "user", "content", prompt)
                    ),
                    "temperature", aiConfig.getTemperature(),
                    "max_tokens", 400
            );

            String responseBody = restClient.post()
                    .uri(GROQ_API_URL)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + aiConfig.getGroqApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(requestPayload)
                    .exchange((clientRequest, clientResponse) -> {
                        int statusCode = clientResponse.getStatusCode().value();
                        if (statusCode == 429) {
                            log.warn("Groq provider returned HTTP 429.");
                            return null;
                        }
                        if (!clientResponse.getStatusCode().is2xxSuccessful()) {
                            log.warn("[AI-FLOW] provider=groq model={} success=false reason={}", model, statusCode);
                            try {
                                InputStream stream = clientResponse.getBody();
                                byte[] bytes = stream != null ? stream.readAllBytes() : new byte[0];
                                String err = sanitize(new String(bytes, StandardCharsets.UTF_8));
                                log.warn("Groq API error status [{}] for model [{}]: {}", statusCode, model, err);
                            } catch (Exception streamEx) {
                                log.warn("Groq API error status [{}] for model [{}]", statusCode, model);
                            }
                            return null;
                        }
                        InputStream stream = clientResponse.getBody();
                        byte[] bytes = stream != null ? stream.readAllBytes() : new byte[0];
                        return new String(bytes, StandardCharsets.UTF_8);
                    });

            if (responseBody == null || responseBody.isBlank()) {
                return null;
            }

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode choices = root.path("choices");
            if (choices.isArray() && !choices.isEmpty()) {
                JsonNode message = choices.get(0).path("message");
                String content = message.path("content").asText("");
                if (!content.isBlank()) {
                    return cleanResponseText(content);
                }
            }

            log.warn("Groq response missing choices/content for model [{}].", model);
            return null;

        } catch (Exception e) {
            String sanitizedError = sanitize(e.getMessage() != null ? e.getMessage() : "Unknown error");
            log.warn("Groq request for model [{}] failed: {}", model, sanitizedError);
            return null;
        }
    }

    @Override
    public String getProviderName() {
        return isAvailable() ? "groq" : "fallback";
    }

    @Override
    public boolean isAvailable() {
        return aiConfig.isGroqConfigured();
    }

    private String cleanResponseText(String text) {
        if (text == null) return "";
        String trimmed = text.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() > 2) {
            trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
        }
        return trimmed;
    }

    private String sanitize(String input) {
        if (input == null) return "";
        return input.replaceAll("Bearer\\s+[A-Za-z0-9_\\-\\.]+", "Bearer REDACTED")
                .replaceAll("gsk_[A-Za-z0-9]+", "REDACTED");
    }
}
