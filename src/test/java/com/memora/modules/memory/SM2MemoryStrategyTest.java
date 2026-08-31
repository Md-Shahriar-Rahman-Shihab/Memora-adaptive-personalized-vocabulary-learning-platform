package com.memora.modules.memory;

import com.memora.modules.memory.domain.MemoryAlgorithmType;
import com.memora.modules.memory.domain.MemoryCalculationResult;
import com.memora.modules.memory.domain.MemoryInput;
import com.memora.modules.memory.strategy.SM2MemoryStrategy;
import com.memora.modules.vocabulary.domain.ForgettingRisk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class SM2MemoryStrategyTest {

    private SM2MemoryStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new SM2MemoryStrategy();
    }

    @Test
    @DisplayName("Should return SM2 algorithm type")
    void shouldReturnSM2Type() {
        assertThat(strategy.getAlgorithmType()).isEqualTo(MemoryAlgorithmType.SM2);
    }

    @Test
    @DisplayName("First correct answer should yield 1-day interval and initial mastery")
    void firstCorrectAnswerYieldsOneDayInterval() {
        MemoryInput input = MemoryInput.builder()
                .totalAttempts(1)
                .correctAttempts(1)
                .incorrectAttempts(0)
                .consecutiveCorrect(1)
                .consecutiveIncorrect(0)
                .averageResponseTime(1500.0)
                .lastResponseTimeMs(1500)
                .lastAttemptCorrect(true)
                .currentMasteryScore(0.0)
                .currentForgettingRisk(ForgettingRisk.HIGH)
                .build();

        MemoryCalculationResult result = strategy.calculate(input);

        assertThat(result).isNotNull();
        assertThat(result.getReviewIntervalDays()).isEqualTo(1);
        assertThat(result.getMasteryScore()).isGreaterThan(50.0);
        assertThat(result.getNextReviewAt()).isAfter(Instant.now());
    }

    @Test
    @DisplayName("Second consecutive correct answer should yield 3-day interval")
    void secondCorrectAnswerYieldsThreeDayInterval() {
        MemoryInput input = MemoryInput.builder()
                .totalAttempts(2)
                .correctAttempts(2)
                .incorrectAttempts(0)
                .consecutiveCorrect(2)
                .consecutiveIncorrect(0)
                .averageResponseTime(1600.0)
                .lastResponseTimeMs(1600)
                .lastAttemptCorrect(true)
                .currentMasteryScore(60.0)
                .currentForgettingRisk(ForgettingRisk.MEDIUM)
                .build();

        MemoryCalculationResult result = strategy.calculate(input);

        assertThat(result.getReviewIntervalDays()).isEqualTo(3);
        assertThat(result.getMasteryScore()).isGreaterThan(65.0);
    }

    @Test
    @DisplayName("Repeated consecutive correct answers should expand interval and reduce forgetting risk to LOW")
    void repeatedCorrectAnswersExpandIntervalAndLowerRisk() {
        MemoryInput input = MemoryInput.builder()
                .totalAttempts(4)
                .correctAttempts(4)
                .incorrectAttempts(0)
                .consecutiveCorrect(4)
                .consecutiveIncorrect(0)
                .averageResponseTime(1200.0)
                .lastResponseTimeMs(1200)
                .lastAttemptCorrect(true)
                .currentMasteryScore(80.0)
                .currentForgettingRisk(ForgettingRisk.MEDIUM)
                .build();

        MemoryCalculationResult result = strategy.calculate(input);

        assertThat(result.getReviewIntervalDays()).isGreaterThan(3);
        assertThat(result.getMasteryScore()).isGreaterThanOrEqualTo(80.0);
        assertThat(result.getForgettingRisk()).isEqualTo(ForgettingRisk.LOW);
    }

    @Test
    @DisplayName("Incorrect answer should reset interval to 1 day and increase forgetting risk")
    void incorrectAnswerResetsIntervalTo1Day() {
        MemoryInput input = MemoryInput.builder()
                .totalAttempts(3)
                .correctAttempts(2)
                .incorrectAttempts(1)
                .consecutiveCorrect(0)
                .consecutiveIncorrect(1)
                .averageResponseTime(3500.0)
                .lastResponseTimeMs(4000)
                .lastAttemptCorrect(false)
                .currentMasteryScore(75.0)
                .currentForgettingRisk(ForgettingRisk.LOW)
                .build();

        MemoryCalculationResult result = strategy.calculate(input);

        assertThat(result.getReviewIntervalDays()).isEqualTo(1);
        assertThat(result.getMasteryScore()).isLessThan(75.0);
    }

    @Test
    @DisplayName("Repeated incorrect answers should escalate forgetting risk to HIGH")
    void repeatedIncorrectAnswersEscalateRiskToHigh() {
        MemoryInput input = MemoryInput.builder()
                .totalAttempts(5)
                .correctAttempts(1)
                .incorrectAttempts(4)
                .consecutiveCorrect(0)
                .consecutiveIncorrect(3)
                .averageResponseTime(5000.0)
                .lastResponseTimeMs(5000)
                .lastAttemptCorrect(false)
                .currentMasteryScore(30.0)
                .currentForgettingRisk(ForgettingRisk.MEDIUM)
                .build();

        MemoryCalculationResult result = strategy.calculate(input);

        assertThat(result.getReviewIntervalDays()).isEqualTo(1);
        assertThat(result.getForgettingRisk()).isEqualTo(ForgettingRisk.HIGH);
    }
}
