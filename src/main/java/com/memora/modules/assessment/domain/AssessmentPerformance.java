package com.memora.modules.assessment.domain;

import com.memora.modules.vocabulary.domain.DifficultyLevel;

/**
 * Value object capturing a learner's granular performance metrics within a specific CEFR level.
 */
public class AssessmentPerformance {

    private final DifficultyLevel level;
    private final int totalQuestions;
    private final int correctAnswers;
    private final double accuracy;
    private final double averageResponseTimeMs;

    public AssessmentPerformance(DifficultyLevel level, int totalQuestions, int correctAnswers,
                                 double accuracy, double averageResponseTimeMs) {
        this.level = level;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.accuracy = accuracy;
        this.averageResponseTimeMs = averageResponseTimeMs;
    }

    public DifficultyLevel getLevel() {
        return level;
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

    public double getAverageResponseTimeMs() {
        return averageResponseTimeMs;
    }
}
