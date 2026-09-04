package com.memora.modules.gamification.rule;

import com.memora.modules.gamification.domain.AchievementCode;
import com.memora.modules.gamification.domain.AchievementEvaluationContext;
import org.springframework.stereotype.Component;

/**
 * Rule: Unlocked when the learner has learned at least 50 words.
 */
@Component
public class VocabularyExplorerAchievementRule implements AchievementRule {

    public static final int THRESHOLD = 50;

    @Override
    public String getAchievementCode() {
        return AchievementCode.VOCABULARY_EXPLORER;
    }

    @Override
    public boolean evaluate(AchievementEvaluationContext context) {
        if (context == null) return false;
        return context.getTotalWordsLearned() >= THRESHOLD;
    }
}
