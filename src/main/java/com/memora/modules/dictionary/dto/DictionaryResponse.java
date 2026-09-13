package com.memora.modules.dictionary.dto;

import java.util.List;

/**
 * Unified, stable Memora dictionary response DTO representing verified headword information,
 * pronunciation, official audio URL, categorized parts of speech, concise short definitions,
 * etymology, and spelling suggestions.
 */
public record DictionaryResponse(
        String word,
        String headword,
        String pronunciation,
        String audioUrl,
        List<PartOfSpeechDto> partsOfSpeech,
        List<String> shortDefinitions,
        String etymology,
        List<String> suggestions
) {
    public DictionaryResponse {
        if (partsOfSpeech == null) {
            partsOfSpeech = List.of();
        }
        if (shortDefinitions == null) {
            shortDefinitions = List.of();
        }
        if (suggestions == null) {
            suggestions = List.of();
        }
    }
}
