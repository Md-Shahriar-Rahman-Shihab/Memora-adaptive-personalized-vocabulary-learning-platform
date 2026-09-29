package com.memora.modules.dictionary.dto;

import java.util.List;
import java.util.Map;

/**
 * Unified, stable Memora dictionary response DTO representing verified headword information,
 * pronunciation, official audio URL, categorized parts of speech, concise short definitions,
 * etymology, spelling suggestions, synonyms, antonyms, and word family relations.
 */
public record DictionaryResponse(
        String word,
        String headword,
        String pronunciation,
        String audioUrl,
        List<PartOfSpeechDto> partsOfSpeech,
        List<String> shortDefinitions,
        String etymology,
        List<String> suggestions,
        List<String> synonyms,
        List<String> antonyms,
        Map<String, String> wordFamily
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

    /**
     * Backward-compatible 8-argument constructor for existing callers and tests.
     */
    public DictionaryResponse(
            String word,
            String headword,
            String pronunciation,
            String audioUrl,
            List<PartOfSpeechDto> partsOfSpeech,
            List<String> shortDefinitions,
            String etymology,
            List<String> suggestions
    ) {
        this(word, headword, pronunciation, audioUrl, partsOfSpeech, shortDefinitions, etymology, suggestions, List.of(), List.of(), Map.of());
    }
}
