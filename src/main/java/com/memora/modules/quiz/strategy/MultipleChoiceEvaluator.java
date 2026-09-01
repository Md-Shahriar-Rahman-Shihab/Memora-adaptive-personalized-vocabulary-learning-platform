package com.memora.modules.quiz.strategy;

import com.memora.modules.quiz.domain.QuestionType;
import com.memora.modules.quiz.dto.EvaluationResult;
import com.memora.modules.quiz.entity.MultipleChoiceQuestion;
import com.memora.modules.quiz.entity.Question;
import org.springframework.stereotype.Component;

/**
 * Strategy implementation evaluating learner responses for Multiple Choice Questions.
 */
@Component
public class MultipleChoiceEvaluator implements QuestionEvaluatorStrategy {

    @Override
    public QuestionType getSupportedType() {
        return QuestionType.MULTIPLE_CHOICE;
    }

    @Override
    public EvaluationResult evaluate(Question question, String answer) {
        if (!(question instanceof MultipleChoiceQuestion mcq)) {
            throw new IllegalArgumentException("Question is not an instance of MultipleChoiceQuestion");
        }

        if (answer == null || answer.trim().isEmpty()) {
            return new EvaluationResult(false, 0, "No answer provided. The correct option was: " + mcq.getCorrectOption());
        }

        boolean isCorrect = mcq.getCorrectOption().trim().equalsIgnoreCase(answer.trim());
        if (isCorrect) {
            return new EvaluationResult(true, mcq.getPoints(), "Correct! Well done.");
        } else {
            return new EvaluationResult(false, 0, "Incorrect. The correct answer is: " + mcq.getCorrectOption());
        }
    }
}
