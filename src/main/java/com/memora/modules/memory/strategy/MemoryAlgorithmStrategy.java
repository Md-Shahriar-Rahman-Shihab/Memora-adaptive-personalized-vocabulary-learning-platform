package com.memora.modules.memory.strategy;

import com.memora.modules.memory.domain.MemoryAlgorithmType;
import com.memora.modules.memory.domain.MemoryCalculationResult;
import com.memora.modules.memory.domain.MemoryInput;

/**
 * Strategy interface defining the contract for memory retention and spaced repetition algorithms.
 *
 * Demonstrates Strategy Pattern and Open/Closed Principle.
 */
public interface MemoryAlgorithmStrategy {

    /**
     * Identifies the specific algorithm type supported by this strategy implementation.
     *
     * @return {@link MemoryAlgorithmType}
     */
    MemoryAlgorithmType getAlgorithmType();

    /**
     * Calculates the updated mastery score, forgetting risk, and next review schedule
     * based on learner historical metrics and current response data.
     *
     * @param input Decoupled memory input metrics
     * @return {@link MemoryCalculationResult} with newly calculated memory state
     */
    MemoryCalculationResult calculate(MemoryInput input);
}
