package com.memora.modules.ai;

import com.memora.modules.ai.provider.FallbackAiProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FallbackAiProviderTest {

    private final FallbackAiProvider provider = new FallbackAiProvider();

    @Test
    @DisplayName("Should always be available and have fallback name")
    void testProviderIdentity() {
        assertEquals("fallback", provider.getProviderName());
        assertTrue(provider.isAvailable());
    }

    @Test
    @DisplayName("Should generate deterministic content without external calls")
    void testGenerateContent() {
        String result = provider.generateContent("Explain the word rapid");
        assertNotNull(result);
        assertFalse(result.isBlank());
        assertTrue(result.contains("Deterministic educational"));
    }
}
