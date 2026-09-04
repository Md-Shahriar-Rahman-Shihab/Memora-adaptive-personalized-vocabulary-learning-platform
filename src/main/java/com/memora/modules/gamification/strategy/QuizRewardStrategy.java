package com.memora.modules.gamification.strategy;

import com.memora.modules.gamification.domain.RewardActivityType;
import com.memora.modules.gamification.domain.RewardContext;
import org.springframework.stereotype.Component;

/**
 * Calculates XP rewards for quiz interactions:
 * - Correct answer: +10 XP
 * - Perfect quiz score bonus: +20 XP
 */
@Component
public class QuizRewardStrategy implements RewardStrategy {

    public static final int CORRECT_ANSWER_XP = 10;
    public static final int PERFECT_QUIZ_BONUS_XP = 20;

    @Override
    public RewardActivityType getActivityType() {
        return RewardActivityType.QUIZ;
    }

    @Override
    public int calculateReward(RewardContext context) {
        if (context == null) return 0;

        int xp = 0;
        if (Boolean.TRUE.equals(context.getIsCorrect())) {
            xp += CORRECT_ANSWER_XP;
        }

        if (Boolean.TRUE.equals(context.getIsPerfect())) {
            xp += PERFECT_QUIZ_BONUS_XP;
        }

        return xp;
    }
}
