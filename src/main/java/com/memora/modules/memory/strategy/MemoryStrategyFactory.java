package com.memora.modules.memory.strategy;

import com.memora.common.exception.BadRequestException;
import com.memora.modules.memory.domain.MemoryAlgorithmType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory component responsible for resolving {@link MemoryAlgorithmStrategy} instances.
 *
 * Demonstrates the Factory Pattern and Dependency Inversion Principle by dynamically
 * registering all strategy beans and decoupling the service layer from concrete implementations.
 */
@Component
public class MemoryStrategyFactory {

    private final Map<MemoryAlgorithmType, MemoryAlgorithmStrategy> strategies;

    public MemoryStrategyFactory(List<MemoryAlgorithmStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toUnmodifiableMap(
                        MemoryAlgorithmStrategy::getAlgorithmType,
                        Function.identity()
                ));
    }

    /**
     * Resolves the corresponding strategy implementation for the given algorithm type.
     *
     * @param type {@link MemoryAlgorithmType}
     * @return Concrete {@link MemoryAlgorithmStrategy}
     * @throws BadRequestException if the type is null or unsupported
     */
    public MemoryAlgorithmStrategy getStrategy(MemoryAlgorithmType type) {
        if (type == null) {
            throw new BadRequestException("Memory algorithm type must not be null");
        }
        MemoryAlgorithmStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new BadRequestException("Unsupported memory algorithm: " + type);
        }
        return strategy;
    }
}
