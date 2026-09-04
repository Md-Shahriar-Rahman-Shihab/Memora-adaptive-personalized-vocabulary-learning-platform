package com.memora.modules.gamification.rule;

import com.memora.modules.gamification.domain.AchievementCode;
import com.memora.modules.gamification.domain.AchievementEvaluationContext;
import org.springframework.stereotype.Component;

/**
 * Rule: Unlocked when the learner has completed at least 10 quizzes.
 */
@Component
public class QuizMasterAchievementRule implements AchievementRule {

    public static final int THRESHOLD = 10;

    @Override
    public String getAchievementCode() {
        return AchievementCode.QUIZ_MASTER;
    }

    @Override
    public boolean evaluate(AchievementEvaluationContext context) {
        if (context == null) return false;
        return context.getTotalQuizzesCompleted() >= THRESHOLD;
    }
}
