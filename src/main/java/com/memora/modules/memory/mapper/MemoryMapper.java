package com.memora.modules.memory.mapper;

import com.memora.modules.memory.domain.MemoryAlgorithmType;
import com.memora.modules.memory.domain.MemoryCalculationResult;
import com.memora.modules.memory.dto.MemoryWordResponse;
import com.memora.modules.memory.dto.WordReviewResponse;
import com.memora.modules.vocabulary.entity.UserWordProgress;
import com.memora.modules.vocabulary.entity.VocabularyWord;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Mapper for translating Memory domain models and entities into API response DTOs.
 */
@Component
public class MemoryMapper {

    public WordReviewResponse toReviewResponse(UserWordProgress progress,
                                               boolean correct,
                                               MemoryAlgorithmType algorithm,
                                               MemoryCalculationResult result) {
        return toReviewResponse(progress, correct, algorithm, result, null, null, null, null, null);
    }

    public WordReviewResponse toReviewResponse(UserWordProgress progress,
                                               boolean correct,
                                               MemoryAlgorithmType algorithm,
                                               MemoryCalculationResult result,
                                               Integer xpEarned,
                                               Integer currentStreak,
                                               Integer totalXp,
                                               Double previousMasteryScore,
                                               com.memora.modules.vocabulary.domain.ForgettingRisk previousForgettingRisk) {
        if (progress == null) {
            return null;
        }

        VocabularyWord word = progress.getVocabularyWord();
        Long wordId = word != null ? word.getId() : null;
        String wordText = word != null ? word.getWord() : null;

        return new WordReviewResponse(
                wordId,
                wordText,
                correct,
                result.getMasteryScore(),
                result.getForgettingRisk(),
                result.getNextReviewAt(),
                result.getReviewIntervalDays(),
                algorithm,
                xpEarned,
                currentStreak,
                totalXp,
                previousMasteryScore,
                previousForgettingRisk
        );
    }

    public MemoryWordResponse toMemoryWordResponse(UserWordProgress progress) {
        if (progress == null) {
            return null;
        }

        VocabularyWord word = progress.getVocabularyWord();
        return new MemoryWordResponse(
                word != null ? word.getId() : null,
                word != null ? word.getWord() : null,
                word != null ? word.getMeaning() : null,
                word != null ? word.getDifficultyLevel() : null,
                word != null ? word.getCategory() : null,
                progress.getMasteryScore(),
                progress.getForgettingRisk(),
                progress.getNextReviewAt(),
                progress.getLastReviewedAt(),
                progress.getTotalAttempts(),
                progress.getCorrectAttempts(),
                progress.getIncorrectAttempts(),
                progress.getConsecutiveCorrect(),
                progress.getAverageResponseTime(),
                progress.getLeitnerBox(),
                word != null ? word.getDefinition() : null,
                word != null ? word.getPronunciation() : null,
                word != null ? word.getExampleSentence() : null
        );
    }

    public List<MemoryWordResponse> toMemoryWordResponseList(List<UserWordProgress> progressList) {
        if (progressList == null || progressList.isEmpty()) {
            return Collections.emptyList();
        }
        return progressList.stream()
                .map(this::toMemoryWordResponse)
                .toList();
    }
}
