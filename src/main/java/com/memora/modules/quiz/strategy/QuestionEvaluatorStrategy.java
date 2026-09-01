package com.memora.modules.quiz.strategy;

import com.memora.modules.quiz.domain.QuestionType;
import com.memora.modules.quiz.dto.EvaluationResult;
import com.memora.modules.quiz.entity.Question;

/**
 * Strategy interface defining answer evaluation abstraction for polymorphic question types.
 */
public interface QuestionEvaluatorStrategy {

    /**
     * @return The {@link QuestionType} handled by this strategy.
     */
    QuestionType getSupportedType();

    /**
     * Evaluates learner answer against the specific question structure.
     *
     * @param question Concrete Question instance
     * @param answer Submitted answer text
     * @return {@link EvaluationResult}
     */
    EvaluationResult evaluate(Question question, String answer);
}
