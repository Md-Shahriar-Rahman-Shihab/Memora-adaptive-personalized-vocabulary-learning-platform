package com.memora.modules.ai.provider;

import com.memora.modules.ai.config.AiConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Centralized AI provider router orchestrating provider selection and graceful fallback
 * according to the configured {@code MEMORA_AI_PROVIDER} mode (auto, gemini, groq, fallback).
 *
 * <p>Routing policies:
 * <ul>
 *   <li><b>auto</b> (default): Gemini (single attempt) &rarr; Groq (single attempt) &rarr; Deterministic Fallback.
 *       If Gemini succeeds, Groq is never invoked. Maximum cloud attempts per request is strictly 2.</li>
 *   <li><b>gemini</b>: Gemini &rarr; Deterministic Fallback (Groq is never invoked).</li>
 *   <li><b>groq</b>: Groq &rarr; Deterministic Fallback (Gemini is never invoked).</li>
 *   <li><b>fallback</b>: Deterministic Fallback directly.</li>
 * </ul>
 */
@Primary
@Component("aiProviderRouter")
public class AiProviderRouter implements AiProvider {

    private static final Logger log = LoggerFactory.getLogger(AiProviderRouter.class);

    private final AiConfig aiConfig;
    private final GeminiAiProvider geminiAiProvider;
    private final GroqAiProvider groqAiProvider;
    private final FallbackAiProvider fallbackAiProvider;

    public AiProviderRouter(
            AiConfig aiConfig,
            GeminiAiProvider geminiAiProvider,
            GroqAiProvider groqAiProvider,
            FallbackAiProvider fallbackAiProvider) {
        this.aiConfig = aiConfig;
        this.geminiAiProvider = geminiAiProvider;
        this.groqAiProvider = groqAiProvider;
        this.fallbackAiProvider = fallbackAiProvider;
    }

    @Override
    public AiGenerationResult generate(String prompt) {
        String mode = aiConfig.getProvider();

        switch (mode) {
            case "gemini" -> {
                log.info("[ROUTER] Explicit Gemini mode active.");
                AiGenerationResult geminiResult = geminiAiProvider.generate(prompt);
                if (geminiResult != null && !geminiResult.isFallback()) {
                    return geminiResult;
                }
                log.info("Deterministic AI fallback used.");
                return fallbackAiProvider.generate(prompt);
            }

            case "groq" -> {
                log.info("[ROUTER] Explicit Groq mode active.");
                AiGenerationResult groqResult = groqAiProvider.generate(prompt);
                if (groqResult != null && !groqResult.isFallback()) {
                    return groqResult;
                }
                log.info("Deterministic AI fallback used.");
                return fallbackAiProvider.generate(prompt);
            }

            case "fallback" -> {
                log.info("Deterministic AI fallback used.");
                return fallbackAiProvider.generate(prompt);
            }

            case "auto" -> {
                return executeAutoFallbackChain(prompt);
            }

            default -> {
                log.warn("[ROUTER] Unrecognized AI provider mode [{}]. Using AUTO fallback chain.", mode);
                return executeAutoFallbackChain(prompt);
            }
        }
    }

    private AiGenerationResult executeAutoFallbackChain(String prompt) {
        // Step 1: Attempt Gemini (single attempt, no retry loops)
        if (geminiAiProvider.isAvailable()) {
            AiGenerationResult geminiResult = geminiAiProvider.generateDirect(prompt);
            if (geminiResult != null && !geminiResult.isFallback()
                    && geminiResult.text() != null && !geminiResult.text().isBlank()) {
                log.info("[ROUTER] Gemini succeeded on primary attempt.");
                return geminiResult;
            }
            log.info("Gemini provider unavailable; attempting Groq fallback.");
        } else {
            log.info("Gemini provider not configured; attempting Groq fallback.");
        }

        // Step 2: Fallback to Groq (single attempt)
        if (groqAiProvider.isAvailable()) {
            AiGenerationResult groqResult = groqAiProvider.generateDirect(prompt);
            if (groqResult != null && !groqResult.isFallback()
                    && groqResult.text() != null && !groqResult.text().isBlank()) {
                log.info("[ROUTER] Groq fallback succeeded.");
                return groqResult;
            }
            log.info("Groq provider unavailable; falling back to deterministic fallback.");
        } else {
            log.info("Groq provider not configured; falling back to deterministic fallback.");
        }

        // Step 3: Final safety layer - deterministic educational fallback
        log.info("Deterministic AI fallback used.");
        return fallbackAiProvider.generate(prompt);
    }

    @Override
    public String generateContent(String prompt) {
        return generate(prompt).text();
    }

    @Override
    public String getProviderName() {
        return aiConfig.getProvider();
    }

    @Override
    public boolean isAvailable() {
        String mode = aiConfig.getProvider();
        if ("gemini".equalsIgnoreCase(mode)) {
            return geminiAiProvider.isAvailable();
        }
        if ("groq".equalsIgnoreCase(mode)) {
            return groqAiProvider.isAvailable();
        }
        if ("auto".equalsIgnoreCase(mode)) {
            return geminiAiProvider.isAvailable() || groqAiProvider.isAvailable();
        }
        return true;
    }
}
