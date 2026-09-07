package com.memora.modules.ai.dto;

/**
 * Response payload containing contextual example sentence.
 */
public record AiExampleResponse(
        String word,
        String cefrLevel,
        String exampleSentence,
        String context,
        String provider,
        String model,
        boolean isFallback,
        boolean cached
) {
    public AiExampleResponse(String word, String cefrLevel, String exampleSentence, String context, String provider, boolean isFallback, boolean cached) {
        this(word, cefrLevel, exampleSentence, context, provider, null, isFallback, cached);
    }
}
