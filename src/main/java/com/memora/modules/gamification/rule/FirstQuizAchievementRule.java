package com.memora.modules.gamification.rule;

import com.memora.modules.gamification.domain.AchievementCode;
import com.memora.modules.gamification.domain.AchievementEvaluationContext;
import org.springframework.stereotype.Component;

/**
 * Rule: Unlocked when the learner completes their first quiz.
 */
@Component
public class FirstQuizAchievementRule implements AchievementRule {

    @Override
    public String getAchievementCode() {
        return AchievementCode.FIRST_QUIZ;
    }

    @Override
    public boolean evaluate(AchievementEvaluationContext context) {
        if (context == null) return false;
        return context.getTotalQuizzesCompleted() >= 1;
    }
}
