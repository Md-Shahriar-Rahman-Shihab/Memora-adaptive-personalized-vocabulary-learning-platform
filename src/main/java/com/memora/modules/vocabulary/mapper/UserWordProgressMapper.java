package com.memora.modules.vocabulary.mapper;

import com.memora.modules.vocabulary.dto.UserWordProgressResponse;
import com.memora.modules.vocabulary.entity.UserWordProgress;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Mapper component for translating {@link UserWordProgress} entities to {@link UserWordProgressResponse} DTOs.
 * Calculates dynamic computed metrics such as accuracy percentage.
 */
@Component
public class UserWordProgressMapper {

    public UserWordProgressResponse toResponse(UserWordProgress entity) {
        if (entity == null) {
            return null;
        }

        double accuracy = 0.0;
        if (entity.getTotalAttempts() > 0) {
            accuracy = Math.round(((double) entity.getCorrectAttempts() / entity.getTotalAttempts()) * 10000.0) / 100.0;
        }

        Long wordId = entity.getVocabularyWord() != null ? entity.getVocabularyWord().getId() : null;
        String wordText = entity.getVocabularyWord() != null ? entity.getVocabularyWord().getWord() : null;

        return new UserWordProgressResponse(
                wordId,
                wordText,
                entity.getTotalAttempts(),
                entity.getCorrectAttempts(),
                entity.getIncorrectAttempts(),
                accuracy,
                entity.getAverageResponseTime(),
                entity.getMasteryScore(),
                entity.getForgettingRisk(),
                entity.getLastReviewedAt(),
                entity.getNextReviewAt()
        );
    }

    public List<UserWordProgressResponse> toResponseList(List<UserWordProgress> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(this::toResponse)
                .toList();
    }
}
