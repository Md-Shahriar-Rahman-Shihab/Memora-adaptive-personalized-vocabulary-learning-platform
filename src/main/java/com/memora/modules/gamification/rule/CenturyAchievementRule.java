package com.memora.modules.gamification.rule;

import com.memora.modules.gamification.domain.AchievementCode;
import com.memora.modules.gamification.domain.AchievementEvaluationContext;
import org.springframework.stereotype.Component;

/**
 * Rule: Unlocked when the learner has learned at least 100 words.
 */
@Component
public class CenturyAchievementRule implements AchievementRule {

    public static final int THRESHOLD = 100;

    @Override
    public String getAchievementCode() {
        return AchievementCode.CENTURY;
    }

    @Override
    public boolean evaluate(AchievementEvaluationContext context) {
        if (context == null) return false;
        return context.getTotalWordsLearned() >= THRESHOLD;
    }
}
