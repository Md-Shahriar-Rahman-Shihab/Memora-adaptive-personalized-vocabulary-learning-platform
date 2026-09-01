package com.memora.modules.quiz.dto;

/**
 * Output payload summarizing final score, correctness, and percentage for a completed QuizAttempt.
 */
public class QuizResultResponse {

    private Long quizId;
    private int totalQuestions;
    private int correctAnswers;
    private int totalScore;
    private double percentage;

    public QuizResultResponse() {
    }

    public QuizResultResponse(Long quizId, int totalQuestions, int correctAnswers, int totalScore, double percentage) {
        this.quizId = quizId;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.totalScore = totalScore;
        this.percentage = percentage;
    }

    public Long getQuizId() {
        return quizId;
    }

    public void setQuizId(Long quizId) {
        this.quizId = quizId;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }
}
