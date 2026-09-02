package com.memora.modules.assessment.domain;

import com.memora.modules.vocabulary.domain.DifficultyLevel;

import java.util.Collections;
import java.util.Map;

/**
 * Domain value object representing the output of a CEFR diagnostic placement algorithm.
 */
public class PlacementResult {

    private final DifficultyLevel estimatedLevel;
    private final double confidenceScore;
    private final int totalQuestions;
    private final int correctAnswers;
    private final double accuracy;
    private final Map<DifficultyLevel, Double> levelPerformance;

    public PlacementResult(DifficultyLevel estimatedLevel, double confidenceScore, int totalQuestions,
                           int correctAnswers, double accuracy, Map<DifficultyLevel, Double> levelPerformance) {
        this.estimatedLevel = estimatedLevel;
        this.confidenceScore = confidenceScore;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.accuracy = accuracy;
        this.levelPerformance = levelPerformance != null ? Collections.unmodifiableMap(levelPerformance) : Collections.emptyMap();
    }

    public DifficultyLevel getEstimatedLevel() {
        return estimatedLevel;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public Map<DifficultyLevel, Double> getLevelPerformance() {
        return levelPerformance;
    }
}
