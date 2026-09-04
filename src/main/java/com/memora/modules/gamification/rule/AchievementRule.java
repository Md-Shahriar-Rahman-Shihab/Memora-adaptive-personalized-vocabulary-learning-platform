package com.memora.modules.gamification.rule;

import com.memora.modules.gamification.domain.AchievementEvaluationContext;

/**
 * Strategy/Rule interface defining the contract for evaluating whether a learner qualifies
 * for a specific milestone achievement.
 */
public interface AchievementRule {

    /**
     * Evaluates learner context against this rule's conditions.
     *
     * @param context {@link AchievementEvaluationContext}
     * @return true if the learner meets the criteria
     */
    boolean evaluate(AchievementEvaluationContext context);

    /**
     * @return Unique achievement code matching {@link com.memora.modules.gamification.domain.AchievementCode}.
     */
    String getAchievementCode();
}
