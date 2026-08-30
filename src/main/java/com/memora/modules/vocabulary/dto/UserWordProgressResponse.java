package com.memora.modules.vocabulary.dto;

import com.memora.modules.vocabulary.domain.ForgettingRisk;

import java.time.Instant;

/**
 * Output representation of a learner's progress and retention metrics for a vocabulary word.
 */
public class UserWordProgressResponse {

    private Long vocabularyWordId;
    private String word;
    private int totalAttempts;
    private int correctAttempts;
    private int incorrectAttempts;
    private double accuracy;
    private double averageResponseTime;
    private double masteryScore;
    private ForgettingRisk forgettingRisk;
    private Instant lastReviewedAt;
    private Instant nextReviewAt;

    public UserWordProgressResponse() {
    }

    public UserWordProgressResponse(Long vocabularyWordId, String word, int totalAttempts, int correctAttempts,
                                    int incorrectAttempts, double accuracy, double averageResponseTime,
                                    double masteryScore, ForgettingRisk forgettingRisk,
                                    Instant lastReviewedAt, Instant nextReviewAt) {
        this.vocabularyWordId = vocabularyWordId;
        this.word = word;
        this.totalAttempts = totalAttempts;
        this.correctAttempts = correctAttempts;
        this.incorrectAttempts = incorrectAttempts;
        this.accuracy = accuracy;
        this.averageResponseTime = averageResponseTime;
        this.masteryScore = masteryScore;
        this.forgettingRisk = forgettingRisk;
        this.lastReviewedAt = lastReviewedAt;
        this.nextReviewAt = nextReviewAt;
    }

    public Long getVocabularyWordId() {
        return vocabularyWordId;
    }

    public void setVocabularyWordId(Long vocabularyWordId) {
        this.vocabularyWordId = vocabularyWordId;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getTotalAttempts() {
        return totalAttempts;
    }

    public void setTotalAttempts(int totalAttempts) {
        this.totalAttempts = totalAttempts;
    }

    public int getCorrectAttempts() {
        return correctAttempts;
    }

    public void setCorrectAttempts(int correctAttempts) {
        this.correctAttempts = correctAttempts;
    }

    public int getIncorrectAttempts() {
        return incorrectAttempts;
    }

    public void setIncorrectAttempts(int incorrectAttempts) {
        this.incorrectAttempts = incorrectAttempts;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public double getAverageResponseTime() {
        return averageResponseTime;
    }

    public void setAverageResponseTime(double averageResponseTime) {
        this.averageResponseTime = averageResponseTime;
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

    public Instant getLastReviewedAt() {
        return lastReviewedAt;
    }

    public void setLastReviewedAt(Instant lastReviewedAt) {
        this.lastReviewedAt = lastReviewedAt;
    }

    public Instant getNextReviewAt() {
        return nextReviewAt;
    }

    public void setNextReviewAt(Instant nextReviewAt) {
        this.nextReviewAt = nextReviewAt;
    }
}
