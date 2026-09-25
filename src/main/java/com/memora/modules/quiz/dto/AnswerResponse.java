package com.memora.modules.quiz.dto;

import com.memora.modules.vocabulary.domain.ForgettingRisk;

import java.time.Instant;

/**
 * Output payload returned immediately upon evaluating an individual quiz question answer.
 * Includes score, explanation feedback, and updated adaptive memory retention indicators.
 */
public class AnswerResponse {

    private boolean correct;
    private int score;
    private String feedback;
    private double masteryScore;
    private ForgettingRisk forgettingRisk;
    private Instant nextReviewAt;
    private String correctAnswer;
    private Integer xpEarned;

    public AnswerResponse() {
    }

    public AnswerResponse(boolean correct, int score, String feedback,
                          double masteryScore, ForgettingRisk forgettingRisk, Instant nextReviewAt) {
        this(correct, score, feedback, masteryScore, forgettingRisk, nextReviewAt, null, null);
    }

    public AnswerResponse(boolean correct, int score, String feedback,
                          double masteryScore, ForgettingRisk forgettingRisk, Instant nextReviewAt,
                          String correctAnswer, Integer xpEarned) {
        this.correct = correct;
        this.score = score;
        this.feedback = feedback;
        this.masteryScore = masteryScore;
        this.forgettingRisk = forgettingRisk;
        this.nextReviewAt = nextReviewAt;
        this.correctAnswer = correctAnswer;
        this.xpEarned = xpEarned;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public double getMasteryScore() {
        return masteryScore;
    }

    public void setMasteryScore(double masteryScore) {
        this.masteryScore = masteryScore;
    }

    public ForgettingRisk getForgettingRisk() {
        return forgettingRisk;
    }

    public void setForgettingRisk(ForgettingRisk forgettingRisk) {
        this.forgettingRisk = forgettingRisk;
    }

    public Instant getNextReviewAt() {
        return nextReviewAt;
    }

    public void setNextReviewAt(Instant nextReviewAt) {
        this.nextReviewAt = nextReviewAt;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public Integer getXpEarned() {
        return xpEarned;
    }

    public void setXpEarned(Integer xpEarned) {
        this.xpEarned = xpEarned;
    }
}
