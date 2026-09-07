package com.memora.modules.ai.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Deterministic fallback provider guaranteeing uninterrupted educational assistance
 * when external AI providers are offline, unconfigured, or rate-limited.
 */
@Component("fallbackAiProvider")
public class FallbackAiProvider implements AiProvider {

    private static final Logger log = LoggerFactory.getLogger(FallbackAiProvider.class);

    @Override
    public AiGenerationResult generate(String prompt) {
        return AiGenerationResult.fallback(generateContent(prompt));
    }

    @Override
    public String generateContent(String prompt) {
        log.debug("Generating fallback deterministic response for prompt length: {}", prompt != null ? prompt.length() : 0);
        // Returns safe, educational assistance acknowledgment
        return "Deterministic educational learning assistance provided via Memora Adaptive Memory Engine.";
    }

    @Override
    public String getProviderName() {
        return "fallback";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
