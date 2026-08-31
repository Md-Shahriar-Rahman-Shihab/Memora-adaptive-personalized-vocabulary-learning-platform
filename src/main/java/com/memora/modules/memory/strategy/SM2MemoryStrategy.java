package com.memora.modules.memory.strategy;

import com.memora.modules.memory.domain.MemoryAlgorithmType;
import com.memora.modules.memory.domain.MemoryCalculationResult;
import com.memora.modules.memory.domain.MemoryInput;
import com.memora.modules.vocabulary.domain.ForgettingRisk;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * SuperMemo SM-2 Spaced Repetition Algorithm implementation.
 *
 * Models human memory decay by adjusting review intervals based on accuracy,
 * repetition streaks, and response speed.
 */
@Component
public class SM2MemoryStrategy implements MemoryAlgorithmStrategy {

    private static final double BASE_EASE_FACTOR = 2.5;
    private static final int MAX_INTERVAL_DAYS = 180;

    @Override
    public MemoryAlgorithmType getAlgorithmType() {
        return MemoryAlgorithmType.SM2;
    }

    @Override
    public MemoryCalculationResult calculate(MemoryInput input) {
        Instant now = Instant.now();
        boolean correct = input.isLastAttemptCorrect();
        int totalAttempts = input.getTotalAttempts();
        int correctAttempts = input.getCorrectAttempts();
        int consecutiveCorrect = input.getConsecutiveCorrect();
        int consecutiveIncorrect = input.getConsecutiveIncorrect();
        double avgResponseTime = input.getAverageResponseTime();

        int intervalDays;
        double masteryScore;
        ForgettingRisk forgettingRisk;

        if (correct) {
            // Calculate review interval based on repetition streak
            if (consecutiveCorrect <= 1) {
                intervalDays = 1;
            } else if (consecutiveCorrect == 2) {
                intervalDays = 3;
            } else {
                // Adaptive ease factor calculation
                double easeFactor = BASE_EASE_FACTOR;
                if (input.getLastResponseTimeMs() > 0 && input.getLastResponseTimeMs() <= 2000) {
                    easeFactor += 0.1;
                } else if (input.getLastResponseTimeMs() > 4000) {
                    easeFactor -= 0.15;
                }
                intervalDays = (int) Math.round(3 * Math.pow(easeFactor, consecutiveCorrect - 2));
                intervalDays = Math.min(intervalDays, MAX_INTERVAL_DAYS);
            }

            // Deterministic Mastery Score (0 - 100)
            double accuracyComponent = ((double) correctAttempts / Math.max(1, totalAttempts)) * 60.0;
            double streakComponent = Math.min(30.0, consecutiveCorrect * 6.0);
            double speedComponent = Math.max(0.0, Math.min(10.0, 10.0 - (avgResponseTime / 1000.0)));
            masteryScore = Math.min(100.0, Math.max(0.0, accuracyComponent + streakComponent + speedComponent));

            // Forgetting Risk
            if (consecutiveCorrect >= 3 && masteryScore >= 75.0) {
                forgettingRisk = ForgettingRisk.LOW;
            } else if (consecutiveCorrect >= 1 && masteryScore >= 40.0) {
                forgettingRisk = ForgettingRisk.MEDIUM;
            } else {
                forgettingRisk = ForgettingRisk.HIGH;
            }
        } else {
            // Incorrect answer resets interval to 1 day
            intervalDays = 1;

            double accuracyComponent = ((double) correctAttempts / Math.max(1, totalAttempts)) * 50.0;
            double speedComponent = Math.max(0.0, Math.min(10.0, 10.0 - (avgResponseTime / 1000.0)));
            double calculatedScore = accuracyComponent + speedComponent;
            double penalizedScore = Math.max(0.0, input.getCurrentMasteryScore() - (15.0 * Math.max(1, consecutiveIncorrect)));
            masteryScore = Math.min(100.0, Math.max(0.0, Math.min(calculatedScore, penalizedScore)));

            if (consecutiveIncorrect >= 2 || masteryScore < 40.0) {
                forgettingRisk = ForgettingRisk.HIGH;
            } else {
                forgettingRisk = ForgettingRisk.MEDIUM;
            }
        }

        masteryScore = Math.round(masteryScore * 100.0) / 100.0;
        Instant nextReviewAt = now.plus(Duration.ofDays(intervalDays));

        return MemoryCalculationResult.builder()
                .masteryScore(masteryScore)
                .forgettingRisk(forgettingRisk)
                .nextReviewAt(nextReviewAt)
                .reviewIntervalDays(intervalDays)
                .updatedLeitnerBox(input.getCurrentLeitnerBox())
                .build();
    }
}
