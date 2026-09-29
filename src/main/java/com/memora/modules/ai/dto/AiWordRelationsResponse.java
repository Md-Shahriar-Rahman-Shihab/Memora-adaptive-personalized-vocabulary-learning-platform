package com.memora.modules.ai.dto;

import java.util.List;
import java.util.Map;

/**
 * Output representation of AI-generated or fallback-derived linguistic relationships,
 * covering relevant synonyms, antonyms, and word family grammatical derivatives.
 */
public record AiWordRelationsResponse(
        String word,
        List<String> synonyms,
        List<String> antonyms,
        Map<String, String> wordFamily,
        String provider,
        String model,
        boolean isFallback,
        boolean cached
) {
    public AiWordRelationsResponse {
        if (synonyms == null) {
            synonyms = List.of();
        }
        if (antonyms == null) {
            antonyms = List.of();
        }
        if (wordFamily == null) {
            wordFamily = Map.of();
        }
    }
}
