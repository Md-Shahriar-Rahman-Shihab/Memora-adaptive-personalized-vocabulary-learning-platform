package com.memora.modules.learningpath.factory;

import com.memora.modules.learningpath.domain.LearningPathStrategyType;
import com.memora.modules.learningpath.strategy.AdaptiveLearningPathStrategy;
import com.memora.modules.learningpath.strategy.LearningPathStrategy;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Factory pattern managing and resolving {@link LearningPathStrategy} implementations.
 * Enables adding future rule-based or AI-driven adaptive strategies without modifying service clients.
 */
@Component
public class LearningPathStrategyFactory {

    private final Map<LearningPathStrategyType, LearningPathStrategy> strategyMap = new EnumMap<>(LearningPathStrategyType.class);
    private final AdaptiveLearningPathStrategy defaultStrategy;

    public LearningPathStrategyFactory(List<LearningPathStrategy> strategies,
                                       AdaptiveLearningPathStrategy defaultStrategy) {
        this.defaultStrategy = defaultStrategy;
        for (LearningPathStrategy strategy : strategies) {
            strategyMap.put(strategy.getStrategyType(), strategy);
        }
    }

    /**
     * Resolves strategy by explicit type.
     */
    public LearningPathStrategy getStrategy(LearningPathStrategyType type) {
        if (type == null) {
            return defaultStrategy;
        }
        LearningPathStrategy strategy = strategyMap.get(type);
        if (strategy == null) {
            return defaultStrategy;
        }
        return strategy;
    }

    /**
     * @return Default adaptive learning path strategy.
     */
    public LearningPathStrategy getDefaultStrategy() {
        return defaultStrategy;
    }
}
