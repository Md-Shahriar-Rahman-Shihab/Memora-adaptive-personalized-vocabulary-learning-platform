package com.memora.modules.quiz.strategy;

import com.memora.modules.quiz.domain.QuestionType;
import com.memora.modules.quiz.dto.EvaluationResult;
import com.memora.modules.quiz.entity.FillInTheBlankQuestion;
import com.memora.modules.quiz.entity.Question;
import org.springframework.stereotype.Component;

/**
 * Strategy implementation evaluating learner responses for Fill In The Blank Questions.
 */
@Component
public class FillInTheBlankEvaluator implements QuestionEvaluatorStrategy {

    @Override
    public QuestionType getSupportedType() {
        return QuestionType.FILL_IN_THE_BLANK;
    }

    @Override
    public EvaluationResult evaluate(Question question, String answer) {
        if (!(question instanceof FillInTheBlankQuestion fib)) {
            throw new IllegalArgumentException("Question is not an instance of FillInTheBlankQuestion");
        }

        if (answer == null || answer.trim().isEmpty()) {
            return new EvaluationResult(false, 0, "No answer provided. The expected word was: " + fib.getExpectedAnswer());
        }

        boolean isCorrect = fib.getExpectedAnswer().trim().equalsIgnoreCase(answer.trim());
        if (isCorrect) {
            return new EvaluationResult(true, fib.getPoints(), "Correct! You completed the sentence accurately.");
        } else {
            return new EvaluationResult(false, 0, "Incorrect. The expected word was: " + fib.getExpectedAnswer());
        }
    }
}
