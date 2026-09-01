package com.memora.modules.quiz.factory;

import com.memora.modules.quiz.domain.QuestionType;
import com.memora.modules.quiz.strategy.QuestionEvaluatorStrategy;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Factory pattern managing and resolving {@link QuestionEvaluatorStrategy} instances based on {@link QuestionType}.
 * Eliminates conditional logic in calling services and maintains the Open/Closed Principle.
 */
@Component
public class QuestionEvaluatorFactory {

    private final Map<QuestionType, QuestionEvaluatorStrategy> evaluators = new EnumMap<>(QuestionType.class);

    public QuestionEvaluatorFactory(List<QuestionEvaluatorStrategy> strategyList) {
        if (strategyList != null) {
            for (QuestionEvaluatorStrategy strategy : strategyList) {
                evaluators.put(strategy.getSupportedType(), strategy);
            }
        }
    }

    /**
     * Resolves the appropriate evaluator strategy for the given question type.
     *
     * @param type Target {@link QuestionType}
     * @return {@link QuestionEvaluatorStrategy}
     */
    public QuestionEvaluatorStrategy getEvaluator(QuestionType type) {
        QuestionEvaluatorStrategy strategy = evaluators.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("No evaluator registered for question type: " + type);
        }
        return strategy;
    }
}
