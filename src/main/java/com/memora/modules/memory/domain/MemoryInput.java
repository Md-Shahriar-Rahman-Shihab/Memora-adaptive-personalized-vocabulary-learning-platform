package com.memora.modules.memory.domain;

import com.memora.modules.vocabulary.domain.ForgettingRisk;

import java.time.Instant;

/**
 * Value object encapsulating learner performance metrics, attempt history,
 * and current retention state required for memory calculations.
 *
 * Fully decouples memory calculation strategies from JPA entity structures.
 */
public class MemoryInput {

    private final int totalAttempts;
    private final int correctAttempts;
    private final int incorrectAttempts;
    private final double averageResponseTime;
    private final int consecutiveCorrect;
    private final int consecutiveIncorrect;
    private final Instant lastReviewedAt;
    private final double currentMasteryScore;
    private final ForgettingRisk currentForgettingRisk;
    private final Instant currentNextReviewAt;
    private final int currentLeitnerBox;
    private final boolean lastAttemptCorrect;
    private final long lastResponseTimeMs;

    public MemoryInput(int totalAttempts,
                       int correctAttempts,
                       int incorrectAttempts,
                       double averageResponseTime,
                       int consecutiveCorrect,
                       int consecutiveIncorrect,
                       Instant lastReviewedAt,
                       double currentMasteryScore,
                       ForgettingRisk currentForgettingRisk,
                       Instant currentNextReviewAt,
                       int currentLeitnerBox,
                       boolean lastAttemptCorrect,
                       long lastResponseTimeMs) {
        this.totalAttempts = totalAttempts;
        this.correctAttempts = correctAttempts;
        this.incorrectAttempts = incorrectAttempts;
        this.averageResponseTime = averageResponseTime;
        this.consecutiveCorrect = consecutiveCorrect;
        this.consecutiveIncorrect = consecutiveIncorrect;
        this.lastReviewedAt = lastReviewedAt;
        this.currentMasteryScore = currentMasteryScore;
        this.currentForgettingRisk = currentForgettingRisk;
        this.currentNextReviewAt = currentNextReviewAt;
        this.currentLeitnerBox = currentLeitnerBox;
        this.lastAttemptCorrect = lastAttemptCorrect;
        this.lastResponseTimeMs = lastResponseTimeMs;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getTotalAttempts() {
        return totalAttempts;
    }

    public int getCorrectAttempts() {
        return correctAttempts;
    }

    public int getIncorrectAttempts() {
        return incorrectAttempts;
    }

    public double getAverageResponseTime() {
        return averageResponseTime;
    }

    public int getConsecutiveCorrect() {
        return consecutiveCorrect;
    }

    public int getConsecutiveIncorrect() {
        return consecutiveIncorrect;
    }

    public Instant getLastReviewedAt() {
        return lastReviewedAt;
    }

    public double getCurrentMasteryScore() {
        return currentMasteryScore;
    }

    public ForgettingRisk getCurrentForgettingRisk() {
        return currentForgettingRisk;
    }

    public Instant getCurrentNextReviewAt() {
        return currentNextReviewAt;
    }

    public int getCurrentLeitnerBox() {
        return currentLeitnerBox;
    }

    public boolean isLastAttemptCorrect() {
        return lastAttemptCorrect;
    }

    public long getLastResponseTimeMs() {
        return lastResponseTimeMs;
    }

    public static class Builder {
        private int totalAttempts;
        private int correctAttempts;
        private int incorrectAttempts;
        private double averageResponseTime;
        private int consecutiveCorrect;
        private int consecutiveIncorrect;
        private Instant lastReviewedAt;
        private double currentMasteryScore;
        private ForgettingRisk currentForgettingRisk = ForgettingRisk.LOW;
        private Instant currentNextReviewAt;
        private int currentLeitnerBox = 1;
        private boolean lastAttemptCorrect;
        private long lastResponseTimeMs;

        public Builder totalAttempts(int totalAttempts) {
            this.totalAttempts = totalAttempts;
            return this;
        }

        public Builder correctAttempts(int correctAttempts) {
            this.correctAttempts = correctAttempts;
            return this;
        }

        public Builder incorrectAttempts(int incorrectAttempts) {
            this.incorrectAttempts = incorrectAttempts;
            return this;
        }

        public Builder averageResponseTime(double averageResponseTime) {
            this.averageResponseTime = averageResponseTime;
            return this;
        }

        public Builder consecutiveCorrect(int consecutiveCorrect) {
            this.consecutiveCorrect = consecutiveCorrect;
            return this;
        }

        public Builder consecutiveIncorrect(int consecutiveIncorrect) {
            this.consecutiveIncorrect = consecutiveIncorrect;
            return this;
        }

        public Builder lastReviewedAt(Instant lastReviewedAt) {
            this.lastReviewedAt = lastReviewedAt;
            return this;
        }

        public Builder currentMasteryScore(double currentMasteryScore) {
            this.currentMasteryScore = currentMasteryScore;
            return this;
        }

        public Builder currentForgettingRisk(ForgettingRisk currentForgettingRisk) {
            this.currentForgettingRisk = currentForgettingRisk;
            return this;
        }

        public Builder currentNextReviewAt(Instant currentNextReviewAt) {
            this.currentNextReviewAt = currentNextReviewAt;
            return this;
        }

        public Builder currentLeitnerBox(int currentLeitnerBox) {
            this.currentLeitnerBox = currentLeitnerBox;
            return this;
        }

        public Builder lastAttemptCorrect(boolean lastAttemptCorrect) {
            this.lastAttemptCorrect = lastAttemptCorrect;
            return this;
        }

        public Builder lastResponseTimeMs(long lastResponseTimeMs) {
            this.lastResponseTimeMs = lastResponseTimeMs;
            return this;
        }

        public MemoryInput build() {
            return new MemoryInput(
                    totalAttempts,
                    correctAttempts,
                    incorrectAttempts,
                    averageResponseTime,
                    consecutiveCorrect,
                    consecutiveIncorrect,
                    lastReviewedAt,
                    currentMasteryScore,
                    currentForgettingRisk,
                    currentNextReviewAt,
                    currentLeitnerBox,
                    lastAttemptCorrect,
                    lastResponseTimeMs
            );
        }
    }
}
