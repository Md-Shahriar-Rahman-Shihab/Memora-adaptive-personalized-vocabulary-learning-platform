package com.memora.modules.gamification.strategy;

import com.memora.modules.gamification.domain.RewardActivityType;
import com.memora.modules.gamification.domain.RewardContext;
import org.springframework.stereotype.Component;

/**
 * Calculates XP rewards for concluding a diagnostic placement assessment: +50 XP.
 */
@Component
public class AssessmentRewardStrategy implements RewardStrategy {

    public static final int ASSESSMENT_COMPLETION_XP = 50;

    @Override
    public RewardActivityType getActivityType() {
        return RewardActivityType.ASSESSMENT;
    }

    @Override
    public int calculateReward(RewardContext context) {
        return ASSESSMENT_COMPLETION_XP;
    }
}
