package com.memora.modules.ai.dto;

/**
 * Response payload containing AI-generated mnemonic or retention tip.
 */
public record AiMemoryTipResponse(
        String word,
        String cefrLevel,
        String memoryTip,
        String association,
        String provider,
        String model,
        boolean isFallback,
        boolean cached
) {
    public AiMemoryTipResponse(String word, String cefrLevel, String memoryTip, String association, String provider, boolean isFallback, boolean cached) {
        this(word, cefrLevel, memoryTip, association, provider, null, isFallback, cached);
    }
}
