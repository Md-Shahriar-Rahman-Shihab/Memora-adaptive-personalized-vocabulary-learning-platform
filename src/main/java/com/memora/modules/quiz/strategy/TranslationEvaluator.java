package com.memora.modules.quiz.strategy;

import com.memora.modules.quiz.domain.QuestionType;
import com.memora.modules.quiz.dto.EvaluationResult;
import com.memora.modules.quiz.entity.Question;
import com.memora.modules.quiz.entity.TranslationQuestion;
import org.springframework.stereotype.Component;

/**
 * Strategy implementation evaluating learner responses for Translation Questions.
 */
@Component
public class TranslationEvaluator implements QuestionEvaluatorStrategy {

    @Override
    public QuestionType getSupportedType() {
        return QuestionType.TRANSLATION;
    }

    @Override
    public EvaluationResult evaluate(Question question, String answer) {
        if (!(question instanceof TranslationQuestion tq)) {
            throw new IllegalArgumentException("Question is not an instance of TranslationQuestion");
        }

        if (answer == null || answer.trim().isEmpty()) {
            return new EvaluationResult(false, 0, "No answer provided. The expected translation was: " + tq.getExpectedAnswer());
        }

        boolean isCorrect = tq.getExpectedAnswer().trim().equalsIgnoreCase(answer.trim());
        if (isCorrect) {
            return new EvaluationResult(true, tq.getPoints(), "Correct translation! Well done.");
        } else {
            return new EvaluationResult(false, 0, "Incorrect. The expected translation is: " + tq.getExpectedAnswer());
        }
    }
}
