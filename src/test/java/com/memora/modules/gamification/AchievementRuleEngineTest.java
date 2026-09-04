package com.memora.modules.gamification;

import com.memora.modules.gamification.domain.AchievementCode;
import com.memora.modules.gamification.domain.AchievementEvaluationContext;
import com.memora.modules.gamification.domain.RewardActivityType;
import com.memora.modules.gamification.entity.UserGamificationProfile;
import com.memora.modules.gamification.rule.*;
import com.memora.modules.user.domain.Role;
import com.memora.modules.user.domain.VocabularyLevel;
import com.memora.modules.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AchievementRuleEngineTest {

    private AchievementRuleEngine ruleEngine;
    private User testUser;
    private UserGamificationProfile testProfile;

    @BeforeEach
    void setUp() {
        testUser = new User("Learner", "learner@memora.com", "hash", VocabularyLevel.B1, Role.LEARNER);
        testProfile = new UserGamificationProfile(testUser);

        ruleEngine = new AchievementRuleEngine(List.of(
                new FirstLessonAchievementRule(),
                new FirstQuizAchievementRule(),
                new WordStarterAchievementRule(),
                new VocabularyExplorerAchievementRule(),
                new CenturyAchievementRule(),
                new PerfectScoreAchievementRule(),
                new QuizMasterAchievementRule(),
                new SevenDayStreakAchievementRule(),
                new ThirtyDayStreakAchievementRule(),
                new MemoryMasterAchievementRule()
        ));
    }

    @Test
    @DisplayName("First lesson activity should unlock FIRST_LESSON achievement")
    void testFirstLessonRule() {
        AchievementEvaluationContext context = new AchievementEvaluationContext(
                testUser, testProfile, 1, 0, 0, 1, false, 1, RewardActivityType.LESSON
        );

        List<String> unlocked = ruleEngine.evaluateEligibleAchievements(context);
        assertTrue(unlocked.contains(AchievementCode.FIRST_LESSON));
        assertFalse(unlocked.contains(AchievementCode.FIRST_QUIZ));
    }

    @Test
    @DisplayName("Vocabulary milestones should unlock at 10, 50, and 100 words")
    void testVocabularyMilestoneRules() {
        // 10 words
        AchievementEvaluationContext context10 = new AchievementEvaluationContext(
                testUser, testProfile, 10, 0, 0, 10, false, 1, RewardActivityType.LESSON
        );
        List<String> unlocked10 = ruleEngine.evaluateEligibleAchievements(context10);
        assertTrue(unlocked10.contains(AchievementCode.WORD_STARTER));
        assertFalse(unlocked10.contains(AchievementCode.VOCABULARY_EXPLORER));

        // 50 words
        AchievementEvaluationContext context50 = new AchievementEvaluationContext(
                testUser, testProfile, 50, 0, 0, 50, false, 1, RewardActivityType.LESSON
        );
        List<String> unlocked50 = ruleEngine.evaluateEligibleAchievements(context50);
        assertTrue(unlocked50.contains(AchievementCode.WORD_STARTER));
        assertTrue(unlocked50.contains(AchievementCode.VOCABULARY_EXPLORER));
        assertFalse(unlocked50.contains(AchievementCode.CENTURY));

        // 100 words
        AchievementEvaluationContext context100 = new AchievementEvaluationContext(
                testUser, testProfile, 100, 0, 0, 100, false, 1, RewardActivityType.LESSON
        );
        List<String> unlocked100 = ruleEngine.evaluateEligibleAchievements(context100);
        assertTrue(unlocked100.contains(AchievementCode.CENTURY));
    }

    @Test
    @DisplayName("Perfect quiz score should unlock PERFECT_SCORE and FIRST_QUIZ")
    void testPerfectScoreRule() {
        AchievementEvaluationContext context = new AchievementEvaluationContext(
                testUser, testProfile, 10, 2, 1, 5, true, 2, RewardActivityType.QUIZ
        );

        List<String> unlocked = ruleEngine.evaluateEligibleAchievements(context);
        assertTrue(unlocked.contains(AchievementCode.FIRST_QUIZ));
        assertTrue(unlocked.contains(AchievementCode.PERFECT_SCORE));
        assertFalse(unlocked.contains(AchievementCode.QUIZ_MASTER));
    }

    @Test
    @DisplayName("Streak milestones should unlock STREAK_7 and STREAK_30")
    void testStreakRules() {
        AchievementEvaluationContext context7 = new AchievementEvaluationContext(
                testUser, testProfile, 20, 5, 2, 10, false, 7, RewardActivityType.REVIEW
        );
        List<String> unlocked7 = ruleEngine.evaluateEligibleAchievements(context7);
        assertTrue(unlocked7.contains(AchievementCode.STREAK_7));
        assertFalse(unlocked7.contains(AchievementCode.STREAK_30));

        AchievementEvaluationContext context30 = new AchievementEvaluationContext(
                testUser, testProfile, 80, 25, 12, 40, false, 30, RewardActivityType.REVIEW
        );
        List<String> unlocked30 = ruleEngine.evaluateEligibleAchievements(context30);
        assertTrue(unlocked30.contains(AchievementCode.STREAK_7));
        assertTrue(unlocked30.contains(AchievementCode.STREAK_30));
        assertTrue(unlocked30.contains(AchievementCode.QUIZ_MASTER));
        assertTrue(unlocked30.contains(AchievementCode.MEMORY_MASTER));
    }
}
