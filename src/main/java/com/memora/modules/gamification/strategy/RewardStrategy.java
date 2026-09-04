package com.memora.modules.gamification.strategy;

import com.memora.modules.gamification.domain.RewardActivityType;
import com.memora.modules.gamification.domain.RewardContext;

/**
 * Strategy interface defining the contract for calculating XP rewards
 * based on specific learning or retention activities.
 */
public interface RewardStrategy {

    /**
     * Computes the XP points awarded for the provided activity context.
     *
     * @param context {@link RewardContext} containing performance flags, scores, and metadata
     * @return non-negative XP amount
     */
    int calculateReward(RewardContext context);

    /**
     * @return The specific {@link RewardActivityType} this strategy handles.
     */
    RewardActivityType getActivityType();
}
