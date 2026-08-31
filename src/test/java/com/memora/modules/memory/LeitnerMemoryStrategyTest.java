package com.memora.modules.memory;

import com.memora.modules.memory.domain.MemoryAlgorithmType;
import com.memora.modules.memory.domain.MemoryCalculationResult;
import com.memora.modules.memory.domain.MemoryInput;
import com.memora.modules.memory.strategy.LeitnerMemoryStrategy;
import com.memora.modules.vocabulary.domain.ForgettingRisk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LeitnerMemoryStrategyTest {

    private LeitnerMemoryStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new LeitnerMemoryStrategy();
    }

    @Test
    @DisplayName("Should return LEITNER algorithm type")
    void shouldReturnLeitnerType() {
        assertThat(strategy.getAlgorithmType()).isEqualTo(MemoryAlgorithmType.LEITNER);
    }

    @Test
    @DisplayName("Correct answer should promote box upward and increase review interval")
    void correctAnswerPromotesBoxUpward() {
        MemoryInput input = MemoryInput.builder()
                .totalAttempts(1)
                .correctAttempts(1)
                .incorrectAttempts(0)
                .currentLeitnerBox(1)
                .lastAttemptCorrect(true)
                .build();

        MemoryCalculationResult result = strategy.calculate(input);

        assertThat(result.getUpdatedLeitnerBox()).isEqualTo(2);
        assertThat(result.getReviewIntervalDays()).isEqualTo(3);
        assertThat(result.getForgettingRisk()).isEqualTo(ForgettingRisk.MEDIUM);
    }

    @Test
    @DisplayName("Higher box promotion reaches Box 5 with 30-day interval and LOW risk")
    void higherBoxPromotionReachesMaxBox() {
        MemoryInput input = MemoryInput.builder()
                .totalAttempts(5)
                .correctAttempts(5)
                .incorrectAttempts(0)
                .currentLeitnerBox(4)
                .lastAttemptCorrect(true)
                .build();

        MemoryCalculationResult result = strategy.calculate(input);

        assertThat(result.getUpdatedLeitnerBox()).isEqualTo(5);
        assertThat(result.getReviewIntervalDays()).isEqualTo(30);
        assertThat(result.getForgettingRisk()).isEqualTo(ForgettingRisk.LOW);
        assertThat(result.getMasteryScore()).isGreaterThanOrEqualTo(90.0);
    }

    @Test
    @DisplayName("Box 5 correct response should stay capped at Box 5")
    void box5CapsAtMax() {
        MemoryInput input = MemoryInput.builder()
                .totalAttempts(10)
                .correctAttempts(10)
                .incorrectAttempts(0)
                .currentLeitnerBox(5)
                .lastAttemptCorrect(true)
                .build();

        MemoryCalculationResult result = strategy.calculate(input);

        assertThat(result.getUpdatedLeitnerBox()).isEqualTo(5);
        assertThat(result.getReviewIntervalDays()).isEqualTo(30);
    }

    @Test
    @DisplayName("Incorrect answer should demote word to lower box and shorten interval")
    void incorrectAnswerDemotesBox() {
        MemoryInput input = MemoryInput.builder()
                .totalAttempts(4)
                .correctAttempts(3)
                .incorrectAttempts(1)
                .currentLeitnerBox(3)
                .lastAttemptCorrect(false)
                .build();

        MemoryCalculationResult result = strategy.calculate(input);

        assertThat(result.getUpdatedLeitnerBox()).isEqualTo(2);
        assertThat(result.getReviewIntervalDays()).isEqualTo(3);
        assertThat(result.getForgettingRisk()).isEqualTo(ForgettingRisk.MEDIUM);
    }

    @Test
    @DisplayName("Incorrect answer at Box 1 should not go below Box 1 and set HIGH risk")
    void incorrectAnswerAtBox1StaysAtBox1() {
        MemoryInput input = MemoryInput.builder()
                .totalAttempts(2)
                .correctAttempts(0)
                .incorrectAttempts(2)
                .currentLeitnerBox(1)
                .lastAttemptCorrect(false)
                .build();

        MemoryCalculationResult result = strategy.calculate(input);

        assertThat(result.getUpdatedLeitnerBox()).isEqualTo(1);
        assertThat(result.getReviewIntervalDays()).isEqualTo(1);
        assertThat(result.getForgettingRisk()).isEqualTo(ForgettingRisk.HIGH);
    }
}
