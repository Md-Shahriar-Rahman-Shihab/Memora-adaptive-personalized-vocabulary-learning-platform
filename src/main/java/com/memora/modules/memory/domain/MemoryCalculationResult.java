package com.memora.modules.memory.domain;

import com.memora.modules.vocabulary.domain.ForgettingRisk;

import java.time.Instant;

/**
 * Encapsulates the output of a memory retention calculation executed by a {@link com.memora.modules.memory.strategy.MemoryAlgorithmStrategy}.
 */
public class MemoryCalculationResult {

    private final double masteryScore;
    private final ForgettingRisk forgettingRisk;
    private final Instant nextReviewAt;
    private final int reviewIntervalDays;
    private final int updatedLeitnerBox;

    public MemoryCalculationResult(double masteryScore,
                                   ForgettingRisk forgettingRisk,
                                   Instant nextReviewAt,
                                   int reviewIntervalDays,
                                   int updatedLeitnerBox) {
        this.masteryScore = masteryScore;
        this.forgettingRisk = forgettingRisk;
        this.nextReviewAt = nextReviewAt;
        this.reviewIntervalDays = reviewIntervalDays;
        this.updatedLeitnerBox = updatedLeitnerBox;
    }

    public static Builder builder() {
        return new Builder();
    }

    public double getMasteryScore() {
        return masteryScore;
    }

    public ForgettingRisk getForgettingRisk() {
        return forgettingRisk;
    }

    public Instant getNextReviewAt() {
        return nextReviewAt;
    }

    public int getReviewIntervalDays() {
        return reviewIntervalDays;
    }

    public int getUpdatedLeitnerBox() {
        return updatedLeitnerBox;
    }

    public static class Builder {
        private double masteryScore;
        private ForgettingRisk forgettingRisk;
        private Instant nextReviewAt;
        private int reviewIntervalDays;
        private int updatedLeitnerBox = 1;

        public Builder masteryScore(double masteryScore) {
            this.masteryScore = masteryScore;
            return this;
        }

        public Builder forgettingRisk(ForgettingRisk forgettingRisk) {
            this.forgettingRisk = forgettingRisk;
            return this;
        }

        public Builder nextReviewAt(Instant nextReviewAt) {
            this.nextReviewAt = nextReviewAt;
            return this;
        }

        public Builder reviewIntervalDays(int reviewIntervalDays) {
            this.reviewIntervalDays = reviewIntervalDays;
            return this;
        }

        public Builder updatedLeitnerBox(int updatedLeitnerBox) {
            this.updatedLeitnerBox = updatedLeitnerBox;
            return this;
        }

        public MemoryCalculationResult build() {
            return new MemoryCalculationResult(
                    masteryScore,
                    forgettingRisk,
                    nextReviewAt,
                    reviewIntervalDays,
                    updatedLeitnerBox
            );
        }
    }
}
