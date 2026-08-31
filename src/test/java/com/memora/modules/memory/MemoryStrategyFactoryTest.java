package com.memora.modules.memory;

import com.memora.common.exception.BadRequestException;
import com.memora.modules.memory.domain.MemoryAlgorithmType;
import com.memora.modules.memory.strategy.LeitnerMemoryStrategy;
import com.memora.modules.memory.strategy.MemoryAlgorithmStrategy;
import com.memora.modules.memory.strategy.MemoryStrategyFactory;
import com.memora.modules.memory.strategy.SM2MemoryStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemoryStrategyFactoryTest {

    private MemoryStrategyFactory factory;
    private SM2MemoryStrategy sm2Strategy;
    private LeitnerMemoryStrategy leitnerStrategy;

    @BeforeEach
    void setUp() {
        sm2Strategy = new SM2MemoryStrategy();
        leitnerStrategy = new LeitnerMemoryStrategy();
        factory = new MemoryStrategyFactory(List.of(sm2Strategy, leitnerStrategy));
    }

    @Test
    @DisplayName("Should resolve SM2 strategy correctly without instanceof checks")
    void shouldResolveSM2Strategy() {
        MemoryAlgorithmStrategy strategy = factory.getStrategy(MemoryAlgorithmType.SM2);

        assertThat(strategy).isNotNull();
        assertThat(strategy).isSameAs(sm2Strategy);
        assertThat(strategy.getAlgorithmType()).isEqualTo(MemoryAlgorithmType.SM2);
    }

    @Test
    @DisplayName("Should resolve LEITNER strategy correctly")
    void shouldResolveLeitnerStrategy() {
        MemoryAlgorithmStrategy strategy = factory.getStrategy(MemoryAlgorithmType.LEITNER);

        assertThat(strategy).isNotNull();
        assertThat(strategy).isSameAs(leitnerStrategy);
        assertThat(strategy.getAlgorithmType()).isEqualTo(MemoryAlgorithmType.LEITNER);
    }

    @Test
    @DisplayName("Should throw BadRequestException when algorithm type is null")
    void shouldThrowWhenAlgorithmTypeIsNull() {
        assertThatThrownBy(() -> factory.getStrategy(null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Memory algorithm type must not be null");
    }
}
