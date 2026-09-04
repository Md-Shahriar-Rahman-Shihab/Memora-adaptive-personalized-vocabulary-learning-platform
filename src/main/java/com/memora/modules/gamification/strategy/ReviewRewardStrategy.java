package com.memora.modules.gamification.strategy;

import com.memora.modules.gamification.domain.RewardActivityType;
import com.memora.modules.gamification.domain.RewardContext;
import org.springframework.stereotype.Component;

/**
 * Calculates XP rewards for completing vocabulary reviews: +5 XP.
 */
@Component
public class ReviewRewardStrategy implements RewardStrategy {

    public static final int REVIEW_COMPLETION_XP = 5;

    @Override
    public RewardActivityType getActivityType() {
        return RewardActivityType.REVIEW;
    }

    @Override
    public int calculateReward(RewardContext context) {
        return REVIEW_COMPLETION_XP;
    }
}
