package com.memora.modules.ai.provider;

/**
 * Encapsulates the text generation result from an AI provider,
 * capturing the exact provider, model name, and fallback status.
 */
public record AiGenerationResult(
        String text,
        String provider,
        String model,
        boolean isFallback
) {
    public static AiGenerationResult success(String text, String provider, String model) {
        return new AiGenerationResult(text, provider, model, false);
    }

    public static AiGenerationResult fallback(String text) {
        return new AiGenerationResult(text, "fallback", null, true);
    }
}
