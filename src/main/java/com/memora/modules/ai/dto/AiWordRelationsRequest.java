package com.memora.modules.ai.dto;

import jakarta.validation.constraints.Size;

/**
 * Request payload for AI-assisted linguistic relationships (synonyms, antonyms, word family).
 */
public record AiWordRelationsRequest(
        Long wordId,
        @Size(max = 100, message = "Word text must not exceed 100 characters")
        String word,
        String cefrLevel
) {
    public boolean isValid() {
        return (wordId != null && wordId > 0) || (word != null && !word.trim().isEmpty());
    }
}
