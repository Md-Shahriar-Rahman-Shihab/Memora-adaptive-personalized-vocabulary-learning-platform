package com.memora.modules.ai.dto;

import java.util.List;

/**
 * Response payload containing contextual usage guidelines and collocations.
 */
public record AiUsageResponse(
        String word,
        String cefrLevel,
        String usageNotes,
        List<String> collocations,
        String register,
        String provider,
        String model,
        boolean isFallback,
        boolean cached
) {
    public AiUsageResponse(String word, String cefrLevel, String usageNotes, List<String> collocations, String register, String provider, boolean isFallback, boolean cached) {
        this(word, cefrLevel, usageNotes, collocations, register, provider, null, isFallback, cached);
    }
}
