package com.memora.modules.ai;

import com.memora.modules.ai.config.AiConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class AiConfigTest {

    @Test
    @DisplayName("Should default to gemini-3.6-flash, openai/gpt-oss-120b, and auto provider")
    void testDefaults() {
        AiConfig config = new AiConfig();
        // Trigger default fields via ReflectionTestUtils or direct access if null
        assertEquals("auto", config.getProvider());
        assertEquals("", config.getApiKey());
        assertEquals("gemini-3.6-flash", config.getModel());
        assertEquals("", config.getGroqApiKey());
        assertEquals("openai/gpt-oss-120b", config.getGroqModel());
        assertEquals(6000, config.getTimeoutMs());
        assertFalse(config.isGeminiConfigured());
        assertFalse(config.isGroqConfigured());
        assertFalse(config.isAiConfigured());
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
        assertTrue(config.isAiConfigured());

        // Test diagnostics does not throw
        assertDoesNotThrow(config::logDiagnostics);
    }

    @Test
    @DisplayName("Should support Groq configuration and custom model")
    void testGroqConfiguration() {
        AiConfig config = new AiConfig();
        ReflectionTestUtils.setField(config, "provider", "groq");
        ReflectionTestUtils.setField(config, "groqApiKey", "test-groq-key");
        ReflectionTestUtils.setField(config, "groqModel", "openai/gpt-oss-120b");

        assertEquals("groq", config.getProvider());
        assertEquals("test-groq-key", config.getGroqApiKey());
        assertEquals("openai/gpt-oss-120b", config.getGroqModel());
        assertTrue(config.isGroqConfigured());
        assertTrue(config.isAiConfigured());
    }

    @Test
    @DisplayName("Should fallback model to gemini-3.6-flash if blank or empty")
    void testBlankModelFallback() {
        AiConfig config = new AiConfig();
        ReflectionTestUtils.setField(config, "model", "   ");
        assertEquals("gemini-3.6-flash", config.getModel());

        ReflectionTestUtils.setField(config, "model", null);
        assertEquals("gemini-3.6-flash", config.getModel());

        ReflectionTestUtils.setField(config, "groqModel", "   ");
        assertEquals("openai/gpt-oss-120b", config.getGroqModel());

        ReflectionTestUtils.setField(config, "groqModel", null);
        assertEquals("openai/gpt-oss-120b", config.getGroqModel());
    }
}
