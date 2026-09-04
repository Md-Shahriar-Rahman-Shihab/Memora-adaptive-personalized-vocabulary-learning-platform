package com.memora.modules.gamification.rule;

import com.memora.modules.gamification.domain.AchievementCode;
import com.memora.modules.gamification.domain.AchievementEvaluationContext;
import org.springframework.stereotype.Component;

/**
 * Rule: Unlocked when the learner scores 100% on a quiz.
 */
@Component
public class PerfectScoreAchievementRule implements AchievementRule {

    @Override
    public String getAchievementCode() {
        return AchievementCode.PERFECT_SCORE;
    }

    @Override
    public boolean evaluate(AchievementEvaluationContext context) {
        if (context == null) return false;
        return context.isPerfectQuiz();
    }
}
