package com.memora.modules.gamification.strategy;

import com.memora.modules.gamification.domain.RewardActivityType;
import com.memora.modules.gamification.domain.RewardContext;
import org.springframework.stereotype.Component;

/**
 * Calculates XP rewards for completing new vocabulary discovery lesson items: +15 XP.
 */
@Component
public class LessonRewardStrategy implements RewardStrategy {

    public static final int LESSON_COMPLETION_XP = 15;

    @Override
    public RewardActivityType getActivityType() {
        return RewardActivityType.LESSON;
    }

    @Override
    public int calculateReward(RewardContext context) {
        return LESSON_COMPLETION_XP;
    }
}
