package com.memora.modules.quiz.dto;

/**
 * Encapsulates the output of evaluating a learner's answer against a polymorphic Question.
 */
public class EvaluationResult {

    private final boolean correct;
    private final int score;
    private final String feedback;

    public EvaluationResult(boolean correct, int score, String feedback) {
        this.correct = correct;
        this.score = score;
        this.feedback = feedback;
    }

    public boolean isCorrect() {
        return correct;
    }

    public int getScore() {
        return score;
    }

    public String getFeedback() {
        return feedback;
    }
}
