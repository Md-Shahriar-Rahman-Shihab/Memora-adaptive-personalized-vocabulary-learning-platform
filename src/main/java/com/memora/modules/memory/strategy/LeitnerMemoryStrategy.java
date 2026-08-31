package com.memora.modules.memory.strategy;

import com.memora.modules.memory.domain.MemoryAlgorithmType;
import com.memora.modules.memory.domain.MemoryCalculationResult;
import com.memora.modules.memory.domain.MemoryInput;
import com.memora.modules.vocabulary.domain.ForgettingRisk;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * 5-Box Leitner Spaced Repetition System strategy implementation.
 *
 * Promotes words to higher interval boxes upon correct answers and demotes
 * them to lower boxes upon incorrect answers.
 */
@Component
public class LeitnerMemoryStrategy implements MemoryAlgorithmStrategy {

    private static final int MIN_BOX = 1;
    private static final int MAX_BOX = 5;

    @Override
    public MemoryAlgorithmType getAlgorithmType() {
        return MemoryAlgorithmType.LEITNER;
    }

    @Override
    public MemoryCalculationResult calculate(MemoryInput input) {
        Instant now = Instant.now();
        boolean correct = input.isLastAttemptCorrect();
        int currentBox = Math.max(MIN_BOX, Math.min(MAX_BOX, input.getCurrentLeitnerBox()));

        int updatedBox;
        if (correct) {
            updatedBox = Math.min(MAX_BOX, currentBox + 1);
        } else {
            updatedBox = Math.max(MIN_BOX, currentBox - 1);
        }

        int intervalDays = getIntervalDaysForBox(updatedBox);
        Instant nextReviewAt = now.plus(Duration.ofDays(intervalDays));

        // Deterministic mastery score calculation based on box + accuracy
        double baseScore = getBaseMasteryScoreForBox(updatedBox);
        double accuracyRatio = input.getTotalAttempts() > 0
                ? (double) input.getCorrectAttempts() / input.getTotalAttempts()
                : 0.5;
        double accuracyModifier = (accuracyRatio - 0.5) * 10.0; // +/- 5.0 points
        double masteryScore = Math.min(100.0, Math.max(0.0, baseScore + accuracyModifier));
        masteryScore = Math.round(masteryScore * 100.0) / 100.0;

        ForgettingRisk forgettingRisk;
        if (updatedBox >= 4) {
            forgettingRisk = ForgettingRisk.LOW;
        } else if (updatedBox >= 2) {
            forgettingRisk = ForgettingRisk.MEDIUM;
        } else {
            forgettingRisk = ForgettingRisk.HIGH;
        }

        return MemoryCalculationResult.builder()
                .masteryScore(masteryScore)
                .forgettingRisk(forgettingRisk)
                .nextReviewAt(nextReviewAt)
                .reviewIntervalDays(intervalDays)
                .updatedLeitnerBox(updatedBox)
                .build();
    }

    private int getIntervalDaysForBox(int box) {
        return switch (box) {
            case 1 -> 1;
            case 2 -> 3;
            case 3 -> 7;
            case 4 -> 14;
            case 5 -> 30;
            default -> 1;
        };
    }

    private double getBaseMasteryScoreForBox(int box) {
        return switch (box) {
            case 1 -> 20.0;
            case 2 -> 40.0;
            case 3 -> 60.0;
            case 4 -> 80.0;
            case 5 -> 95.0;
            default -> 20.0;
        };
    }
}
