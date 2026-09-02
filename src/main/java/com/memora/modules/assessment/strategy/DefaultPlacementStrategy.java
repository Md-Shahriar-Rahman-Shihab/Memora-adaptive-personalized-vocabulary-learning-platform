package com.memora.modules.assessment.strategy;

import com.memora.modules.assessment.domain.AssessmentPerformance;
import com.memora.modules.assessment.domain.PlacementResult;
import com.memora.modules.vocabulary.domain.DifficultyLevel;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Default rule-based deterministic implementation of {@link PlacementAlgorithmStrategy}.
 * Evaluates performance across sequential CEFR difficulty levels (A1 -> A2 -> B1 -> B2 -> C1)
 * to estimate the highest level of demonstrated vocabulary proficiency.
 */
@Component
public class DefaultPlacementStrategy implements PlacementAlgorithmStrategy {

    /**
     * Minimum accuracy threshold (75%) required to demonstrate proficiency at a CEFR level.
     */
    public static final double PROFICIENCY_THRESHOLD = 75.0;

    private static final List<DifficultyLevel> ORDERED_LEVELS = List.of(
            DifficultyLevel.A1,
            DifficultyLevel.A2,
            DifficultyLevel.B1,
            DifficultyLevel.B2,
            DifficultyLevel.C1
    );

    @Override
    public PlacementResult calculate(List<AssessmentPerformance> performances) {
        if (performances == null || performances.isEmpty()) {
            return new PlacementResult(DifficultyLevel.A1, 50.0, 0, 0, 0.0, Collections.emptyMap());
        }

        Map<DifficultyLevel, AssessmentPerformance> perfMap = new EnumMap<>(DifficultyLevel.class);
        Map<DifficultyLevel, Double> levelPerformance = new EnumMap<>(DifficultyLevel.class);

        int totalQuestions = 0;
        int totalCorrect = 0;
        double sumResponseTime = 0.0;
        int countedResponseTimes = 0;

        for (AssessmentPerformance p : performances) {
            perfMap.put(p.getLevel(), p);
            levelPerformance.put(p.getLevel(), Math.round(p.getAccuracy() * 10.0) / 10.0);
            totalQuestions += p.getTotalQuestions();
            totalCorrect += p.getCorrectAnswers();
            if (p.getAverageResponseTimeMs() > 0) {
                sumResponseTime += p.getAverageResponseTimeMs() * p.getTotalQuestions();
                countedResponseTimes += p.getTotalQuestions();
            }
        }

        // Ensure all supported levels exist in the map
        for (DifficultyLevel level : ORDERED_LEVELS) {
            levelPerformance.putIfAbsent(level, 0.0);
        }

        double overallAccuracy = totalQuestions > 0
                ? Math.round(((double) totalCorrect / totalQuestions) * 1000.0) / 10.0
                : 0.0;

        double overallAvgResponseTime = countedResponseTimes > 0
                ? sumResponseTime / countedResponseTimes
                : 0.0;

        // Determine estimated CEFR level: highest level where accuracy >= threshold sequentially
        DifficultyLevel estimatedLevel = DifficultyLevel.A1;
        for (DifficultyLevel level : ORDERED_LEVELS) {
            AssessmentPerformance perf = perfMap.get(level);
            double levelAccuracy = perf != null ? perf.getAccuracy() : 0.0;

            if (levelAccuracy >= PROFICIENCY_THRESHOLD) {
                estimatedLevel = level;
            } else {
                // Stop advancing once a level fails to meet threshold
                break;
            }
        }

        double confidenceScore = calculateConfidenceScore(
                performances,
                levelPerformance,
                estimatedLevel,
                totalQuestions,
                overallAvgResponseTime
        );

        return new PlacementResult(
                estimatedLevel,
                confidenceScore,
                totalQuestions,
                totalCorrect,
                overallAccuracy,
                levelPerformance
        );
    }

    private double calculateConfidenceScore(List<AssessmentPerformance> performances,
                                           Map<DifficultyLevel, Double> levelPerformance,
                                           DifficultyLevel estimatedLevel,
                                           int totalQuestions,
                                           double avgResponseTimeMs) {
        double confidence = 65.0;

        // 1. Completeness factor (up to +15 pts)
        if (totalQuestions >= 20) {
            confidence += 15.0;
        } else if (totalQuestions > 0) {
            confidence += (totalQuestions / 20.0) * 15.0;
        }

        // 2. Monotonic difficulty decay consistency (up to +10 pts)
        // Natural language decay: A1 accuracy >= A2 >= B1 >= B2 >= C1
        double consistencyPoints = 0.0;
        for (int i = 0; i < ORDERED_LEVELS.size() - 1; i++) {
            DifficultyLevel current = ORDERED_LEVELS.get(i);
            DifficultyLevel next = ORDERED_LEVELS.get(i + 1);

            double currentAcc = levelPerformance.getOrDefault(current, 0.0);
            double nextAcc = levelPerformance.getOrDefault(next, 0.0);

            // Bonus if current is greater or within slight margin of next level
            if (currentAcc >= (nextAcc - 10.0)) {
                consistencyPoints += 2.5;
            }
        }
        confidence += consistencyPoints;

        // 3. Response time plausibility factor (+5 pts or penalty)
        if (avgResponseTimeMs >= 800 && avgResponseTimeMs <= 15000) {
            confidence += 5.0;
        } else if (avgResponseTimeMs > 0 && avgResponseTimeMs < 500) {
            // Rapid clicking indicates guessing
            confidence -= 15.0;
        }

        // 4. Clear boundary separation bonus (+5 pts)
        int estimatedIndex = ORDERED_LEVELS.indexOf(estimatedLevel);
        if (estimatedIndex >= 0 && estimatedIndex < ORDERED_LEVELS.size() - 1) {
            DifficultyLevel nextLevel = ORDERED_LEVELS.get(estimatedIndex + 1);
            double currentAcc = levelPerformance.getOrDefault(estimatedLevel, 0.0);
            double nextAcc = levelPerformance.getOrDefault(nextLevel, 0.0);
            if ((currentAcc - nextAcc) >= 15.0) {
                confidence += 5.0;
            }
        }

        // Clamp between 10.0 and 99.0
        return Math.round(Math.min(99.0, Math.max(10.0, confidence)) * 10.0) / 10.0;
    }
}
