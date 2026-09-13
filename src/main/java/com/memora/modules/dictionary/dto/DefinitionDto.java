package com.memora.modules.dictionary.dto;

import java.util.List;

/**
 * Represents a single definition and its corresponding contextual usage examples.
 */
public record DefinitionDto(
        String definition,
        List<String> examples
) {
    public DefinitionDto {
        if (examples == null) {
            examples = List.of();
        }
    }
}
