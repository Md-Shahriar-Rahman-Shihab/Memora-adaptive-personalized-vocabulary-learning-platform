package com.memora.modules.gamification.strategy;

import com.memora.modules.gamification.domain.RewardActivityType;
import com.memora.modules.gamification.domain.RewardContext;
import org.springframework.stereotype.Component;

/**
 * Calculates XP rewards for concluding an entire daily learning path: +30 XP.
 */
@Component
public class DailyPathRewardStrategy implements RewardStrategy {

    public static final int DAILY_PATH_COMPLETION_XP = 30;

    @Override
    public RewardActivityType getActivityType() {
        return RewardActivityType.DAILY_PATH;
    }

    @Override
    public int calculateReward(RewardContext context) {
        return DAILY_PATH_COMPLETION_XP;
    }
}
