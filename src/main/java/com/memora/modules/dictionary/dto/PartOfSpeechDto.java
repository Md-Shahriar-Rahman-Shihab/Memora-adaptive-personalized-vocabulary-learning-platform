package com.memora.modules.dictionary.dto;

import java.util.List;

/**
 * Groups definitions under a distinct grammatical part of speech (e.g. noun, verb, adjective).
 */
public record PartOfSpeechDto(
        String partOfSpeech,
        List<DefinitionDto> definitions
) {
    public PartOfSpeechDto {
        if (definitions == null) {
            definitions = List.of();
        }
    }
}
