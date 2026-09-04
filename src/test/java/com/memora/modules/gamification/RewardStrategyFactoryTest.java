package com.memora.modules.gamification;

import com.memora.modules.gamification.domain.RewardActivityType;
import com.memora.modules.gamification.factory.RewardStrategyFactory;
import com.memora.modules.gamification.strategy.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RewardStrategyFactoryTest {

    private RewardStrategyFactory factory;

    @BeforeEach
    void setUp() {
        factory = new RewardStrategyFactory(List.of(
                new QuizRewardStrategy(),
                new LessonRewardStrategy(),
                new ReviewRewardStrategy(),
                new DailyPathRewardStrategy(),
                new StreakRewardStrategy(),
                new AssessmentRewardStrategy()
        ));
    }

    @Test
    @DisplayName("Factory should correctly resolve all registered strategies by RewardActivityType")
    void testResolveStrategies() {
        RewardStrategy quiz = factory.getStrategy(RewardActivityType.QUIZ);
        assertNotNull(quiz);
        assertEquals(RewardActivityType.QUIZ, quiz.getActivityType());

        RewardStrategy lesson = factory.getStrategy(RewardActivityType.LESSON);
        assertNotNull(lesson);
        assertEquals(RewardActivityType.LESSON, lesson.getActivityType());

        RewardStrategy review = factory.getStrategy(RewardActivityType.REVIEW);
        assertNotNull(review);
        assertEquals(RewardActivityType.REVIEW, review.getActivityType());

        RewardStrategy daily = factory.getStrategy(RewardActivityType.DAILY_PATH);
        assertNotNull(daily);
        assertEquals(RewardActivityType.DAILY_PATH, daily.getActivityType());

        RewardStrategy streak = factory.getStrategy(RewardActivityType.STREAK_BONUS);
        assertNotNull(streak);
        assertEquals(RewardActivityType.STREAK_BONUS, streak.getActivityType());

        RewardStrategy assessment = factory.getStrategy(RewardActivityType.ASSESSMENT);
        assertNotNull(assessment);
        assertEquals(RewardActivityType.ASSESSMENT, assessment.getActivityType());
    }

    @Test
    @DisplayName("Factory should return null when null activity type is provided")
    void testNullActivityType() {
        assertNull(factory.getStrategy(null));
    }
}
