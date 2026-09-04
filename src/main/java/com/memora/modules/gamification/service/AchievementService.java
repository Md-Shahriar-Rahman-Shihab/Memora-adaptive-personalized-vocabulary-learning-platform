package com.memora.modules.gamification.service;

import com.memora.modules.gamification.domain.AchievementEvaluationContext;
import com.memora.modules.gamification.entity.Achievement;
import com.memora.modules.gamification.entity.UserAchievement;
import com.memora.modules.user.entity.User;

import java.util.List;

/**
 * Service contract for evaluating, unlocking, and querying learner achievements.
 */
public interface AchievementService {

    /**
     * Evaluates learner progress and awards any newly unlocked achievements idempotently.
     *
     * @param context {@link AchievementEvaluationContext}
     * @return List of newly awarded {@link Achievement} entities
     */
    List<Achievement> evaluateAndAwardAchievements(AchievementEvaluationContext context);

    /**
     * Retrieves all achievements earned by the learner.
     */
    List<UserAchievement> getLearnerAchievements(User user);

    /**
     * Retrieves all available active achievements in the catalog.
     */
    List<Achievement> getAllActiveAchievements();
}
