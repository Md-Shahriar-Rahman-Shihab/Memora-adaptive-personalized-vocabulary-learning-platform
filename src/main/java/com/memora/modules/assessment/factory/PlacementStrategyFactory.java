package com.memora.modules.assessment.factory;

import com.memora.modules.assessment.strategy.DefaultPlacementStrategy;
import com.memora.modules.assessment.strategy.PlacementAlgorithmStrategy;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Factory pattern managing and resolving {@link PlacementAlgorithmStrategy} implementations.
 * Enables adding future AI-based or adaptive diagnostic placement strategies without altering callers.
 */
@Component
public class PlacementStrategyFactory {

    private final DefaultPlacementStrategy defaultPlacementStrategy;
    private final List<PlacementAlgorithmStrategy> strategies;

    public PlacementStrategyFactory(DefaultPlacementStrategy defaultPlacementStrategy,
                                    List<PlacementAlgorithmStrategy> strategies) {
        this.defaultPlacementStrategy = defaultPlacementStrategy;
        this.strategies = strategies;
    }

    /**
     * @return The default deterministic rule-based placement strategy.
     */
    public PlacementAlgorithmStrategy getDefaultStrategy() {
        return defaultPlacementStrategy;
    }

    /**
     * Resolves a strategy by implementation class type.
     */
    @SuppressWarnings("unchecked")
    public <T extends PlacementAlgorithmStrategy> T getStrategy(Class<T> strategyClass) {
        for (PlacementAlgorithmStrategy s : strategies) {
            if (strategyClass.isInstance(s)) {
                return (T) s;
            }
        }
        return (T) defaultPlacementStrategy;
    }
}
