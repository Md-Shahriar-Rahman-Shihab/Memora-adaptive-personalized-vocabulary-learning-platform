package com.memora.modules.learningpath;

import com.memora.modules.learningpath.domain.LearningPathStrategyType;
import com.memora.modules.learningpath.factory.LearningPathStrategyFactory;
import com.memora.modules.learningpath.strategy.AdaptiveLearningPathStrategy;
import com.memora.modules.learningpath.strategy.LearningPathStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LearningPathStrategyFactoryTest {

    private LearningPathStrategyFactory factory;
    private AdaptiveLearningPathStrategy adaptiveStrategy;

    @BeforeEach
    void setUp() {
        adaptiveStrategy = new AdaptiveLearningPathStrategy();
        factory = new LearningPathStrategyFactory(List.of(adaptiveStrategy), adaptiveStrategy);
    }

    @Test
    @DisplayName("Factory should resolve ADAPTIVE strategy correctly")
    void testResolveAdaptiveStrategy() {
        LearningPathStrategy strategy = factory.getStrategy(LearningPathStrategyType.ADAPTIVE);
        assertNotNull(strategy);
        assertEquals(LearningPathStrategyType.ADAPTIVE, strategy.getStrategyType());
    }

    @Test
    @DisplayName("Factory should return default strategy when null or unrecognized type is requested")
    void testFallbackDefaultStrategy() {
        LearningPathStrategy defaultStrategy = factory.getDefaultStrategy();
        assertNotNull(defaultStrategy);

        assertEquals(defaultStrategy, factory.getStrategy(null));
        assertEquals(defaultStrategy, factory.getStrategy(LearningPathStrategyType.RULE_BASED));
    }
}
