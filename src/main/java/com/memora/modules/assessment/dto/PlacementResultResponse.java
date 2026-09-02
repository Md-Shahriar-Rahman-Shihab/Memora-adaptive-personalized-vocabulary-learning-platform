package com.memora.modules.assessment.dto;

import com.memora.modules.vocabulary.domain.DifficultyLevel;

import java.util.Map;

/**
 * Output payload summarizing a completed diagnostic placement assessment and estimated CEFR proficiency level.
 */
public class PlacementResultResponse {

    private Long assessmentId;
    private DifficultyLevel estimatedLevel;
    private double confidenceScore;
    private int totalQuestions;
    private int correctAnswers;
    private double accuracy;
    private Map<DifficultyLevel, Double> levelPerformance;

    public PlacementResultResponse() {
    }

    public PlacementResultResponse(Long assessmentId, DifficultyLevel estimatedLevel, double confidenceScore,
                                   int totalQuestions, int correctAnswers, double accuracy,
                                   Map<DifficultyLevel, Double> levelPerformance) {
        this.assessmentId = assessmentId;
        this.estimatedLevel = estimatedLevel;
        this.confidenceScore = confidenceScore;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.accuracy = accuracy;
        this.levelPerformance = levelPerformance;
    }

    public Long getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(Long assessmentId) {
        this.assessmentId = assessmentId;
    }

    public DifficultyLevel getEstimatedLevel() {
        return estimatedLevel;
    }

    public void setEstimatedLevel(DifficultyLevel estimatedLevel) {
        this.estimatedLevel = estimatedLevel;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
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

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public Map<DifficultyLevel, Double> getLevelPerformance() {
        return levelPerformance;
    }

    public void setLevelPerformance(Map<DifficultyLevel, Double> levelPerformance) {
        this.levelPerformance = levelPerformance;
    }
}
