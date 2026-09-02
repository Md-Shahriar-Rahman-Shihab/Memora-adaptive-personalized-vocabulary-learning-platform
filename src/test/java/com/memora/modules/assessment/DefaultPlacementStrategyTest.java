package com.memora.modules.assessment;

import com.memora.modules.assessment.domain.AssessmentPerformance;
import com.memora.modules.assessment.domain.PlacementResult;
import com.memora.modules.assessment.strategy.DefaultPlacementStrategy;
import com.memora.modules.vocabulary.domain.DifficultyLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DefaultPlacementStrategyTest {

    private DefaultPlacementStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new DefaultPlacementStrategy();
    }

    @Test
    @DisplayName("Learner with strong A1 and low higher levels should be placed at A1")
    void testStrongA1() {
        List<AssessmentPerformance> performances = List.of(
                new AssessmentPerformance(DifficultyLevel.A1, 4, 4, 100.0, 1500.0),
                new AssessmentPerformance(DifficultyLevel.A2, 4, 2, 50.0, 2000.0),
                new AssessmentPerformance(DifficultyLevel.B1, 4, 1, 25.0, 2500.0),
                new AssessmentPerformance(DifficultyLevel.B2, 4, 0, 0.0, 3000.0),
                new AssessmentPerformance(DifficultyLevel.C1, 4, 0, 0.0, 3200.0)
        );

        PlacementResult result = strategy.calculate(performances);

        assertEquals(DifficultyLevel.A1, result.getEstimatedLevel());
        assertEquals(7, result.getCorrectAnswers());
        assertEquals(20, result.getTotalQuestions());
        assertEquals(35.0, result.getAccuracy());
        assertTrue(result.getConfidenceScore() >= 50.0);
    }

    @Test
    @DisplayName("Learner with strong A1 and A2 should be placed at A2")
    void testStrongA2() {
        List<AssessmentPerformance> performances = List.of(
                new AssessmentPerformance(DifficultyLevel.A1, 4, 4, 100.0, 1200.0),
                new AssessmentPerformance(DifficultyLevel.A2, 4, 4, 100.0, 1400.0),
                new AssessmentPerformance(DifficultyLevel.B1, 4, 2, 50.0, 2200.0),
                new AssessmentPerformance(DifficultyLevel.B2, 4, 1, 25.0, 2800.0),
                new AssessmentPerformance(DifficultyLevel.C1, 4, 0, 0.0, 3500.0)
        );

        PlacementResult result = strategy.calculate(performances);

        assertEquals(DifficultyLevel.A2, result.getEstimatedLevel());
        assertEquals(11, result.getCorrectAnswers());
        assertTrue(result.getConfidenceScore() > 70.0);
    }

    @Test
    @DisplayName("Learner with strong A1, A2, and B1 should be placed at B1")
    void testStrongB1() {
        List<AssessmentPerformance> performances = List.of(
                new AssessmentPerformance(DifficultyLevel.A1, 4, 4, 100.0, 1000.0),
                new AssessmentPerformance(DifficultyLevel.A2, 4, 4, 100.0, 1100.0),
                new AssessmentPerformance(DifficultyLevel.B1, 4, 3, 75.0, 1800.0),
                new AssessmentPerformance(DifficultyLevel.B2, 4, 1, 25.0, 2900.0),
                new AssessmentPerformance(DifficultyLevel.C1, 4, 0, 0.0, 3100.0)
        );

        PlacementResult result = strategy.calculate(performances);

        assertEquals(DifficultyLevel.B1, result.getEstimatedLevel());
        assertTrue(result.getConfidenceScore() > 75.0);
    }

    @Test
    @DisplayName("Learner with strong performance through B2 should be placed at B2")
    void testStrongB2() {
        List<AssessmentPerformance> performances = List.of(
                new AssessmentPerformance(DifficultyLevel.A1, 4, 4, 100.0, 900.0),
                new AssessmentPerformance(DifficultyLevel.A2, 4, 4, 100.0, 1000.0),
                new AssessmentPerformance(DifficultyLevel.B1, 4, 4, 100.0, 1200.0),
                new AssessmentPerformance(DifficultyLevel.B2, 4, 3, 75.0, 1900.0),
                new AssessmentPerformance(DifficultyLevel.C1, 4, 1, 25.0, 3400.0)
        );

        PlacementResult result = strategy.calculate(performances);

        assertEquals(DifficultyLevel.B2, result.getEstimatedLevel());
    }

    @Test
    @DisplayName("Learner with mastery across all levels should be placed at C1")
    void testStrongC1() {
        List<AssessmentPerformance> performances = List.of(
                new AssessmentPerformance(DifficultyLevel.A1, 4, 4, 100.0, 900.0),
                new AssessmentPerformance(DifficultyLevel.A2, 4, 4, 100.0, 950.0),
                new AssessmentPerformance(DifficultyLevel.B1, 4, 4, 100.0, 1100.0),
                new AssessmentPerformance(DifficultyLevel.B2, 4, 4, 100.0, 1300.0),
                new AssessmentPerformance(DifficultyLevel.C1, 4, 4, 100.0, 1600.0)
        );

        PlacementResult result = strategy.calculate(performances);

        assertEquals(DifficultyLevel.C1, result.getEstimatedLevel());
        assertEquals(20, result.getCorrectAnswers());
        assertEquals(100.0, result.getAccuracy());
        assertTrue(result.getConfidenceScore() >= 85.0);
    }

    @Test
    @DisplayName("Learner with overall low performance across all levels should start at foundational A1")
    void testLowPerformance() {
        List<AssessmentPerformance> performances = List.of(
                new AssessmentPerformance(DifficultyLevel.A1, 4, 1, 25.0, 2000.0),
                new AssessmentPerformance(DifficultyLevel.A2, 4, 1, 25.0, 2100.0),
                new AssessmentPerformance(DifficultyLevel.B1, 4, 0, 0.0, 2500.0),
                new AssessmentPerformance(DifficultyLevel.B2, 4, 0, 0.0, 3000.0),
                new AssessmentPerformance(DifficultyLevel.C1, 4, 0, 0.0, 3000.0)
        );

        PlacementResult result = strategy.calculate(performances);

        assertEquals(DifficultyLevel.A1, result.getEstimatedLevel());
        assertEquals(2, result.getCorrectAnswers());
    }

    @Test
    @DisplayName("Boundary testing: Exactly 75.0% satisfies threshold, while 74.9% does not")
    void testBoundaryPercentages() {
        List<AssessmentPerformance> passedA1 = List.of(
                new AssessmentPerformance(DifficultyLevel.A1, 4, 3, 75.0, 1500.0),
                new AssessmentPerformance(DifficultyLevel.A2, 4, 2, 50.0, 1500.0)
        );
        assertEquals(DifficultyLevel.A1, strategy.calculate(passedA1).getEstimatedLevel());

        List<AssessmentPerformance> borderlineA1 = List.of(
                new AssessmentPerformance(DifficultyLevel.A1, 4, 2, 50.0, 1500.0),
                new AssessmentPerformance(DifficultyLevel.A2, 4, 2, 50.0, 1500.0)
        );
        assertEquals(DifficultyLevel.A1, strategy.calculate(borderlineA1).getEstimatedLevel());
    }
}
