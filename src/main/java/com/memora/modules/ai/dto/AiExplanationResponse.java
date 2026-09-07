package com.memora.modules.ai.dto;

/**
 * Response payload containing AI-generated or deterministic fallback vocabulary explanation.
 */
public record AiExplanationResponse(
        String word,
        String cefrLevel,
        String explanation,
        String breakdown,
        String provider,
        String model,
        boolean isFallback,
        boolean cached
) {
    public AiExplanationResponse(String word, String cefrLevel, String explanation, String breakdown, String provider, boolean isFallback, boolean cached) {
        this(word, cefrLevel, explanation, breakdown, provider, null, isFallback, cached);
    }
}
