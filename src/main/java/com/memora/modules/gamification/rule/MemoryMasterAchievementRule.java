package com.memora.modules.gamification.rule;

import com.memora.modules.gamification.domain.AchievementCode;
import com.memora.modules.gamification.domain.AchievementEvaluationContext;
import org.springframework.stereotype.Component;

/**
 * Rule: Unlocked when the learner has mastered at least 20 vocabulary words.
 */
@Component
public class MemoryMasterAchievementRule implements AchievementRule {

    public static final int THRESHOLD = 20;

    @Override
    public String getAchievementCode() {
        return AchievementCode.MEMORY_MASTER;
    }

    @Override
    public boolean evaluate(AchievementEvaluationContext context) {
        if (context == null) return false;
        return context.getTotalMasteredWords() >= THRESHOLD;
    }
}
