package com.memora.modules.assessment.strategy;

import com.memora.modules.assessment.domain.AssessmentPerformance;
import com.memora.modules.assessment.domain.PlacementResult;

import java.util.List;

/**
 * Strategy interface defining the contract for CEFR diagnostic placement algorithms.
 * Allows pluggable placement strategies (rule-based, adaptive, future AI-driven).
 */
public interface PlacementAlgorithmStrategy {

    /**
     * Calculates the estimated CEFR level, confidence score, and metrics from learner performance.
     *
     * @param performances Performance metrics grouped by CEFR difficulty level
     * @return {@link PlacementResult}
     */
    PlacementResult calculate(List<AssessmentPerformance> performances);
}
