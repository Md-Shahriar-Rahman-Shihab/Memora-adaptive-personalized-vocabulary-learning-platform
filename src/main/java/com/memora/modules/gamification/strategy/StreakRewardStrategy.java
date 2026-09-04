package com.memora.modules.gamification.strategy;

import com.memora.modules.gamification.domain.RewardActivityType;
import com.memora.modules.gamification.domain.RewardContext;
import org.springframework.stereotype.Component;

/**
 * Calculates milestone streak bonus rewards:
 * - 7-day streak: +25 XP
 * - 30-day streak: +100 XP
 */
@Component
public class StreakRewardStrategy implements RewardStrategy {

    public static final int STREAK_7_BONUS_XP = 25;
    public static final int STREAK_30_BONUS_XP = 100;

    @Override
    public RewardActivityType getActivityType() {
        return RewardActivityType.STREAK_BONUS;
    }

    @Override
    public int calculateReward(RewardContext context) {
        if (context == null || context.getStreakCount() == null) return 0;

        int streak = context.getStreakCount();
        if (streak == 30) {
            return STREAK_30_BONUS_XP;
        } else if (streak == 7) {
            return STREAK_7_BONUS_XP;
        }
        return 0;
    }
}
