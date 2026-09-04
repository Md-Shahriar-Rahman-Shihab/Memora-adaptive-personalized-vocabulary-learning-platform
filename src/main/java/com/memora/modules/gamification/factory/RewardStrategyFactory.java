package com.memora.modules.gamification.factory;

import com.memora.modules.gamification.domain.RewardActivityType;
import com.memora.modules.gamification.strategy.RewardStrategy;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Factory pattern managing and dynamically resolving {@link RewardStrategy} implementations.
 * Enables adding new reward activities without modifying central conditional logic.
 */
@Component
public class RewardStrategyFactory {

    private final Map<RewardActivityType, RewardStrategy> strategyMap = new EnumMap<>(RewardActivityType.class);

    public RewardStrategyFactory(List<RewardStrategy> strategies) {
        for (RewardStrategy strategy : strategies) {
            strategyMap.put(strategy.getActivityType(), strategy);
        }
    }

    /**
     * Resolves the reward strategy for the given activity type.
     *
     * @param activityType {@link RewardActivityType}
     * @return matching {@link RewardStrategy}, or null if unsupported
     */
    public RewardStrategy getStrategy(RewardActivityType activityType) {
        if (activityType == null) return null;
        return strategyMap.get(activityType);
    }
}
