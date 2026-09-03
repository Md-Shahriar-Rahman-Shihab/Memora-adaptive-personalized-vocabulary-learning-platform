package com.memora.modules.learningpath.domain;

import com.memora.modules.memory.dto.MemoryWordResponse;
import com.memora.modules.vocabulary.domain.DifficultyLevel;

import java.util.*;

/**
 * Context payload supplied to {@link com.memora.modules.learningpath.strategy.LearningPathStrategy}.
 * Decouples strategy algorithms from JPA entity dependencies.
 */
public class LearningPathContext {

    private final DifficultyLevel targetLevel;
    private final List<MemoryWordResponse> dueReviews;
    private final List<MemoryWordResponse> weakWords;
    private final Set<Long> masteredWordIds;
    private final List<VocabularyWordSummary> availableNewWords;
    private final List<VocabularyWordSummary> availableStretchWords;
    private final Double recentAccuracy;
    private final int currentDay;

    public LearningPathContext(DifficultyLevel targetLevel,
                               List<MemoryWordResponse> dueReviews,
                               List<MemoryWordResponse> weakWords,
                               Set<Long> masteredWordIds,
                               List<VocabularyWordSummary> availableNewWords,
                               List<VocabularyWordSummary> availableStretchWords,
                               Double recentAccuracy,
                               int currentDay) {
        this.targetLevel = targetLevel;
        this.dueReviews = dueReviews != null ? List.copyOf(dueReviews) : Collections.emptyList();
        this.weakWords = weakWords != null ? List.copyOf(weakWords) : Collections.emptyList();
        this.masteredWordIds = masteredWordIds != null ? Set.copyOf(masteredWordIds) : Collections.emptySet();
        this.availableNewWords = availableNewWords != null ? List.copyOf(availableNewWords) : Collections.emptyList();
        this.availableStretchWords = availableStretchWords != null ? List.copyOf(availableStretchWords) : Collections.emptyList();
        this.recentAccuracy = recentAccuracy;
        this.currentDay = currentDay;
    }

    public DifficultyLevel getTargetLevel() {
        return targetLevel;
    }

    public List<MemoryWordResponse> getDueReviews() {
        return dueReviews;
    }

    public List<MemoryWordResponse> getWeakWords() {
        return weakWords;
    }

    public Set<Long> getMasteredWordIds() {
        return masteredWordIds;
    }

    public List<VocabularyWordSummary> getAvailableNewWords() {
        return availableNewWords;
    }

    public List<VocabularyWordSummary> getAvailableStretchWords() {
        return availableStretchWords;
    }

    public Double getRecentAccuracy() {
        return recentAccuracy;
    }

    public int getCurrentDay() {
        return currentDay;
    }
}
