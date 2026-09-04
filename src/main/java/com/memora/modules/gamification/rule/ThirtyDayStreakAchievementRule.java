package com.memora.modules.gamification.rule;

import com.memora.modules.gamification.domain.AchievementCode;
import com.memora.modules.gamification.domain.AchievementEvaluationContext;
import org.springframework.stereotype.Component;

/**
 * Rule: Unlocked when the learner maintains a 30-day learning streak.
 */
@Component
public class ThirtyDayStreakAchievementRule implements AchievementRule {

    public static final int THRESHOLD = 30;

    @Override
    public String getAchievementCode() {
        return AchievementCode.STREAK_30;
    }

    @Override
    public boolean evaluate(AchievementEvaluationContext context) {
        if (context == null) return false;
        return context.getCurrentStreak() >= THRESHOLD;
    }
}
