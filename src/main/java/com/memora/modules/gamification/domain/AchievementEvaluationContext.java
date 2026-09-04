package com.memora.modules.gamification.domain;

import com.memora.modules.gamification.entity.UserGamificationProfile;
import com.memora.modules.user.entity.User;

/**
 * Value object carrying live learner statistics and recent activity context
 * to {@link com.memora.modules.gamification.rule.AchievementRule} instances for unlocked badge evaluation.
 */
public class AchievementEvaluationContext {

    private final User user;
    private final UserGamificationProfile profile;
    private final int totalWordsLearned;
    private final int totalMasteredWords;
    private final int totalQuizzesCompleted;
    private final int totalLessonsCompleted;
    private final boolean isPerfectQuiz;
    private final int currentStreak;
    private final RewardActivityType latestActivity;

    public AchievementEvaluationContext(User user,
                                        UserGamificationProfile profile,
                                        int totalWordsLearned,
                                        int totalMasteredWords,
                                        int totalQuizzesCompleted,
                                        int totalLessonsCompleted,
                                        boolean isPerfectQuiz,
                                        int currentStreak,
                                        RewardActivityType latestActivity) {
        this.user = user;
        this.profile = profile;
        this.totalWordsLearned = totalWordsLearned;
        this.totalMasteredWords = totalMasteredWords;
        this.totalQuizzesCompleted = totalQuizzesCompleted;
        this.totalLessonsCompleted = totalLessonsCompleted;
        this.isPerfectQuiz = isPerfectQuiz;
        this.currentStreak = currentStreak;
        this.latestActivity = latestActivity;
    }

    public User getUser() {
        return user;
    }

    public UserGamificationProfile getProfile() {
        return profile;
    }

    public int getTotalWordsLearned() {
        return totalWordsLearned;
    }

    public int getTotalMasteredWords() {
        return totalMasteredWords;
    }

    public int getTotalQuizzesCompleted() {
        return totalQuizzesCompleted;
    }

    public int getTotalLessonsCompleted() {
        return totalLessonsCompleted;
    }

    public boolean isPerfectQuiz() {
        return isPerfectQuiz;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public RewardActivityType getLatestActivity() {
        return latestActivity;
    }
}
