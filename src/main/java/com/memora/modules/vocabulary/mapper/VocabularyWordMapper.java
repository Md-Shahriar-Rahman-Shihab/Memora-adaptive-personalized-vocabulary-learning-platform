package com.memora.modules.vocabulary.mapper;

import com.memora.modules.vocabulary.dto.VocabularyWordRequest;
import com.memora.modules.vocabulary.dto.VocabularyWordResponse;
import com.memora.modules.vocabulary.entity.VocabularyWord;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Mapper component for translating between {@link VocabularyWord} entities and DTOs.
 * Separates data transfer structures from domain logic.
 */
@Component
public class VocabularyWordMapper {

    public VocabularyWord toEntity(VocabularyWordRequest request) {
        if (request == null) {
            return null;
        }
        return new VocabularyWord(
                request.getWord(),
                request.getMeaning(),
                request.getDefinition(),
                request.getPronunciation(),
                request.getExampleSentence(),
                request.getDifficultyLevel(),
                request.getCategory()
        );
    }

    public VocabularyWordResponse toResponse(VocabularyWord entity) {
        if (entity == null) {
            return null;
        }
        return new VocabularyWordResponse(
                entity.getId(),
                entity.getWord(),
                entity.getMeaning(),
                entity.getDefinition(),
                entity.getPronunciation(),
                entity.getExampleSentence(),
                entity.getDifficultyLevel(),
                entity.getCategory()
        );
    }

    public List<VocabularyWordResponse> toResponseList(List<VocabularyWord> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(this::toResponse)
                .toList();
    }
}
