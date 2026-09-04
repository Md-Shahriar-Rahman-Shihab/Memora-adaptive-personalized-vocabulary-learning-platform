package com.memora.modules.gamification.rule;

import com.memora.modules.gamification.domain.AchievementCode;
import com.memora.modules.gamification.domain.AchievementEvaluationContext;
import org.springframework.stereotype.Component;

/**
 * Rule: Unlocked when the learner has learned at least 10 words.
 */
@Component
public class WordStarterAchievementRule implements AchievementRule {

    public static final int THRESHOLD = 10;

    @Override
    public String getAchievementCode() {
        return AchievementCode.WORD_STARTER;
    }

    @Override
    public boolean evaluate(AchievementEvaluationContext context) {
        if (context == null) return false;
        return context.getTotalWordsLearned() >= THRESHOLD;
    }
}
