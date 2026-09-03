package com.memora.modules.learningpath.domain;

import com.memora.modules.vocabulary.domain.DifficultyLevel;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates the output of a {@link com.memora.modules.learningpath.strategy.LearningPathStrategy} execution.
 */
public class LearningPathResult {

    private final DifficultyLevel targetLevel;
    private final List<LearningPathItemCandidate> items;
    private final int totalItems;
    private final Instant generatedAt;
    private final String rationale;

    public LearningPathResult(DifficultyLevel targetLevel, List<LearningPathItemCandidate> items, String rationale) {
        this.targetLevel = targetLevel;
        this.items = items != null ? Collections.unmodifiableList(items) : Collections.emptyList();
        this.totalItems = this.items.size();
        this.generatedAt = Instant.now();
        this.rationale = rationale;
    }

    public DifficultyLevel getTargetLevel() {
        return targetLevel;
    }

    public List<LearningPathItemCandidate> getItems() {
        return items;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public String getRationale() {
        return rationale;
    }
}
