package com.memora.modules.gamification;

import com.memora.modules.gamification.domain.RewardActivityType;
import com.memora.modules.gamification.domain.RewardContext;
import com.memora.modules.gamification.strategy.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RewardStrategyTest {

    @Test
    @DisplayName("QuizRewardStrategy should calculate correct points for standard answer and perfect quiz")
    void testQuizRewardStrategy() {
        QuizRewardStrategy strategy = new QuizRewardStrategy();
        assertEquals(RewardActivityType.QUIZ, strategy.getActivityType());

        // Correct answer only
        RewardContext correctContext = RewardContext.forQuizAnswer(1L, true);
        assertEquals(10, strategy.calculateReward(correctContext));

        // Incorrect answer
        RewardContext incorrectContext = RewardContext.forQuizAnswer(2L, false);
        assertEquals(0, strategy.calculateReward(incorrectContext));

        // Perfect quiz completion
        RewardContext perfectQuizContext = RewardContext.forQuizCompletion(10L, 5, 5, true);
        assertEquals(30, strategy.calculateReward(perfectQuizContext)); // 10 base + 20 bonus
    }

    @Test
    @DisplayName("LessonRewardStrategy should award 15 XP for new vocabulary lesson completion")
    void testLessonRewardStrategy() {
        LessonRewardStrategy strategy = new LessonRewardStrategy();
        assertEquals(RewardActivityType.LESSON, strategy.getActivityType());
        assertEquals(15, strategy.calculateReward(RewardContext.forLesson(100L)));
    }

    @Test
    @DisplayName("ReviewRewardStrategy should award 5 XP for vocabulary review completion")
    void testReviewRewardStrategy() {
        ReviewRewardStrategy strategy = new ReviewRewardStrategy();
        assertEquals(RewardActivityType.REVIEW, strategy.getActivityType());
        assertEquals(5, strategy.calculateReward(RewardContext.forReview(200L)));
    }

    @Test
    @DisplayName("DailyPathRewardStrategy should award 30 XP for concluding daily curriculum")
    void testDailyPathRewardStrategy() {
        DailyPathRewardStrategy strategy = new DailyPathRewardStrategy();
        assertEquals(RewardActivityType.DAILY_PATH, strategy.getActivityType());
        assertEquals(30, strategy.calculateReward(RewardContext.forDailyPath(300L)));
    }

    @Test
    @DisplayName("StreakRewardStrategy should award bonuses at 7 and 30 day milestones")
    void testStreakRewardStrategy() {
        StreakRewardStrategy strategy = new StreakRewardStrategy();
        assertEquals(RewardActivityType.STREAK_BONUS, strategy.getActivityType());

        assertEquals(25, strategy.calculateReward(RewardContext.forStreak(7)));
        assertEquals(100, strategy.calculateReward(RewardContext.forStreak(30)));
        assertEquals(0, strategy.calculateReward(RewardContext.forStreak(5)));
        assertEquals(0, strategy.calculateReward(RewardContext.forStreak(14)));
    }

    @Test
    @DisplayName("AssessmentRewardStrategy should award 50 XP for diagnostic placement completion")
    void testAssessmentRewardStrategy() {
        AssessmentRewardStrategy strategy = new AssessmentRewardStrategy();
        assertEquals(RewardActivityType.ASSESSMENT, strategy.getActivityType());
        assertEquals(50, strategy.calculateReward(RewardContext.forAssessment(400L)));
    }
}
