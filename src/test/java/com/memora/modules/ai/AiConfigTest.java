package com.memora.modules.ai;

import com.memora.modules.ai.config.AiConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class AiConfigTest {

    @Test
    @DisplayName("Should default to gemini-3.6-flash and fallback provider")
    void testDefaults() {
        AiConfig config = new AiConfig();
        // Trigger default fields via ReflectionTestUtils or direct access if null
        assertEquals("fallback", config.getProvider());
        assertEquals("", config.getApiKey());
        assertEquals("gemini-3.6-flash", config.getModel());
        assertEquals(6000, config.getTimeoutMs());
        assertFalse(config.isGeminiConfigured());
    }

    @Test
    @DisplayName("Should use configured custom model if specified")
    void testCustomModel() {
        AiConfig config = new AiConfig();
        ReflectionTestUtils.setField(config, "model", "gemini-3.6-flash");
        ReflectionTestUtils.setField(config, "provider", "gemini");
        ReflectionTestUtils.setField(config, "apiKey", "test-key-12345");

        assertEquals("gemini", config.getProvider());
        assertEquals("test-key-12345", config.getApiKey());
        assertEquals("gemini-3.6-flash", config.getModel());
        assertTrue(config.isGeminiConfigured());

        // Test diagnostics does not throw
        assertDoesNotThrow(config::logDiagnostics);
    }

    @Test
    @DisplayName("Should fallback model to gemini-3.6-flash if blank or empty")
    void testBlankModelFallback() {
        AiConfig config = new AiConfig();
        ReflectionTestUtils.setField(config, "model", "   ");
        assertEquals("gemini-3.6-flash", config.getModel());

        ReflectionTestUtils.setField(config, "model", null);
        assertEquals("gemini-3.6-flash", config.getModel());
    }
}
