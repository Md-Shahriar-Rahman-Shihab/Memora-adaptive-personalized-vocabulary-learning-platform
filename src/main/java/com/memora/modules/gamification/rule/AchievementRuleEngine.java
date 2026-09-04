package com.memora.modules.gamification.rule;

import com.memora.modules.gamification.domain.AchievementEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Orchestrator evaluating all registered {@link AchievementRule} implementations.
 * Enables dynamic discovery and execution of rules without central modifications.
 */
@Component
public class AchievementRuleEngine {

    private final List<AchievementRule> rules;

    public AchievementRuleEngine(List<AchievementRule> rules) {
        this.rules = rules != null ? Collections.unmodifiableList(new ArrayList<>(rules)) : Collections.emptyList();
    }

    /**
     * Evaluates all rules against the provided context and returns the codes of unlocked achievements.
     *
     * @param context {@link AchievementEvaluationContext}
     * @return list of achievement codes that evaluated to true
     */
    public List<String> evaluateEligibleAchievements(AchievementEvaluationContext context) {
        if (context == null) return Collections.emptyList();

        List<String> unlockedCodes = new ArrayList<>();
        for (AchievementRule rule : rules) {
            try {
                if (rule.evaluate(context)) {
                    unlockedCodes.add(rule.getAchievementCode());
                }
            } catch (Exception ignored) {
            }
        }
        return unlockedCodes;
    }
}
