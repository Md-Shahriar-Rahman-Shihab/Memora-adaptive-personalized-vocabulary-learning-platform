package com.memora.modules.learningpath.strategy;

import com.memora.modules.learningpath.domain.LearningPathContext;
import com.memora.modules.learningpath.domain.LearningPathResult;
import com.memora.modules.learningpath.domain.LearningPathStrategyType;

/**
 * Strategy interface defining the contract for personalized curriculum and learning path generation.
 * Enables pluggable algorithms (rule-based, adaptive, future AI-driven).
 */
public interface LearningPathStrategy {

    /**
     * Formulates a balanced, personalized learning path from learner context.
     *
     * @param context {@link LearningPathContext} holding memory state, CEFR level, and vocabulary candidates
     * @return {@link LearningPathResult} containing ordered learning tasks
     */
    LearningPathResult generatePath(LearningPathContext context);

    /**
     * @return The specific strategy type identifier.
     */
    LearningPathStrategyType getStrategyType();
}
