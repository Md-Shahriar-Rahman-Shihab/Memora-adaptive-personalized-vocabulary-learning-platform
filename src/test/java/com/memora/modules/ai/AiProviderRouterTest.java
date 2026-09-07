package com.memora.modules.ai;

import com.memora.modules.ai.config.AiConfig;
import com.memora.modules.ai.provider.AiGenerationResult;
import com.memora.modules.ai.provider.AiProviderRouter;
import com.memora.modules.ai.provider.FallbackAiProvider;
import com.memora.modules.ai.provider.GeminiAiProvider;
import com.memora.modules.ai.provider.GroqAiProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiProviderRouterTest {

    @Mock
    private AiConfig aiConfig;

    @Mock
    private GeminiAiProvider geminiAiProvider;

    @Mock
    private GroqAiProvider groqAiProvider;

    @Mock
    private FallbackAiProvider fallbackAiProvider;

    private AiProviderRouter router;

    @BeforeEach
    void setUp() {
        router = new AiProviderRouter(aiConfig, geminiAiProvider, groqAiProvider, fallbackAiProvider);
    }

    // TEST 1 — Gemini success
    @Test
    @DisplayName("TEST 1: Auto mode - Gemini succeeds, Groq and fallback are never called")
    void test1_geminiSuccess() {
        when(aiConfig.getProvider()).thenReturn("auto");
        when(geminiAiProvider.isAvailable()).thenReturn(true);
        when(geminiAiProvider.generateDirect("Hello")).thenReturn(
                AiGenerationResult.success("Gemini text", "gemini", "gemini-3.6-flash")
        );

        AiGenerationResult result = router.generate("Hello");

        assertNotNull(result);
        assertEquals("Gemini text", result.text());
        assertEquals("gemini", result.provider());
        assertEquals("gemini-3.6-flash", result.model());
        assertFalse(result.isFallback());

        verify(geminiAiProvider, times(1)).generateDirect("Hello");
        verify(groqAiProvider, never()).generate(anyString());
        verify(groqAiProvider, never()).generateDirect(anyString());
        verify(fallbackAiProvider, never()).generate(anyString());
    }

    // TEST 2 — Gemini HTTP 429
    @Test
    @DisplayName("TEST 2: Auto mode - Gemini 429 rate limit falls back to Groq successfully")
    void test2_gemini429_groqSuccess() {
        when(aiConfig.getProvider()).thenReturn("auto");
        when(geminiAiProvider.isAvailable()).thenReturn(true);
        when(geminiAiProvider.generateDirect("Hello")).thenReturn(null); // Represents 429 or failure
        when(groqAiProvider.isAvailable()).thenReturn(true);
        when(groqAiProvider.generateDirect("Hello")).thenReturn(
                AiGenerationResult.success("Groq text", "groq", "openai/gpt-oss-120b")
        );

        AiGenerationResult result = router.generate("Hello");

        assertNotNull(result);
        assertEquals("Groq text", result.text());
        assertEquals("groq", result.provider());
        assertEquals("openai/gpt-oss-120b", result.model());
        assertFalse(result.isFallback());

        verify(geminiAiProvider, times(1)).generateDirect("Hello");
        verify(groqAiProvider, times(1)).generateDirect("Hello");
        verify(fallbackAiProvider, never()).generate(anyString());
    }

    // TEST 3 — Gemini HTTP 503
    @Test
    @DisplayName("TEST 3: Auto mode - Gemini 503 service unavailable falls back to Groq")
    void test3_gemini503_groqSuccess() {
        when(aiConfig.getProvider()).thenReturn("auto");
        when(geminiAiProvider.isAvailable()).thenReturn(true);
        when(geminiAiProvider.generateDirect("Hello")).thenReturn(null); // 503 returns null from executeModelRequest
        when(groqAiProvider.isAvailable()).thenReturn(true);
        when(groqAiProvider.generateDirect("Hello")).thenReturn(
                AiGenerationResult.success("Groq fallback text", "groq", "openai/gpt-oss-120b")
        );

        AiGenerationResult result = router.generate("Hello");

        assertNotNull(result);
        assertEquals("groq", result.provider());
        assertEquals("openai/gpt-oss-120b", result.model());
        verify(geminiAiProvider, times(1)).generateDirect("Hello");
        verify(groqAiProvider, times(1)).generateDirect("Hello");
        verify(fallbackAiProvider, never()).generate(anyString());
    }

    // TEST 4 — Gemini timeout
    @Test
    @DisplayName("TEST 4: Auto mode - Gemini timeout falls back to Groq")
    void test4_geminiTimeout_groqSuccess() {
        when(aiConfig.getProvider()).thenReturn("auto");
        when(geminiAiProvider.isAvailable()).thenReturn(true);
        when(geminiAiProvider.generateDirect("Hello")).thenReturn(null); // Timeout caught and returns null
        when(groqAiProvider.isAvailable()).thenReturn(true);
        when(groqAiProvider.generateDirect("Hello")).thenReturn(
                AiGenerationResult.success("Groq text after timeout", "groq", "openai/gpt-oss-120b")
        );

        AiGenerationResult result = router.generate("Hello");

        assertNotNull(result);
        assertEquals("groq", result.provider());
        assertEquals("openai/gpt-oss-120b", result.model());
        verify(fallbackAiProvider, never()).generate(anyString());
    }

    // TEST 5 — Gemini failure + Groq failure
    @Test
    @DisplayName("TEST 5: Auto mode - Gemini failure + Groq failure returns deterministic fallback")
    void test5_bothFail_returnsFallback() {
        when(aiConfig.getProvider()).thenReturn("auto");
        when(geminiAiProvider.isAvailable()).thenReturn(true);
        when(geminiAiProvider.generateDirect("Hello")).thenReturn(null);
        when(groqAiProvider.isAvailable()).thenReturn(true);
        when(groqAiProvider.generateDirect("Hello")).thenReturn(null);
        when(fallbackAiProvider.generate("Hello")).thenReturn(
                AiGenerationResult.fallback("Deterministic educational fallback")
        );

        AiGenerationResult result = router.generate("Hello");

        assertNotNull(result);
        assertTrue(result.isFallback());
        assertEquals("fallback", result.provider());
        assertNull(result.model());
        assertEquals("Deterministic educational fallback", result.text());

        verify(geminiAiProvider, times(1)).generateDirect("Hello");
        verify(groqAiProvider, times(1)).generateDirect("Hello");
        verify(fallbackAiProvider, times(1)).generate("Hello");
    }

    // TEST 6 — Explicit Gemini mode
    @Test
    @DisplayName("TEST 6: Explicit Gemini mode calls Gemini and never calls Groq even on failure")
    void test6_explicitGeminiMode() {
        when(aiConfig.getProvider()).thenReturn("gemini");
        when(geminiAiProvider.generate("Hello")).thenReturn(
                AiGenerationResult.fallback("Fallback from gemini")
        );
        when(fallbackAiProvider.generate("Hello")).thenReturn(
                AiGenerationResult.fallback("Deterministic fallback")
        );

        AiGenerationResult result = router.generate("Hello");

        assertNotNull(result);
        assertTrue(result.isFallback());
        verify(geminiAiProvider, times(1)).generate("Hello");
        verify(groqAiProvider, never()).generate(anyString());
        verify(groqAiProvider, never()).generateDirect(anyString());
        verify(fallbackAiProvider, times(1)).generate("Hello");
    }

    // TEST 7 — Explicit Groq mode
    @Test
    @DisplayName("TEST 7: Explicit Groq mode calls Groq and never calls Gemini even on failure")
    void test7_explicitGroqMode() {
        when(aiConfig.getProvider()).thenReturn("groq");
        when(groqAiProvider.generate("Hello")).thenReturn(
                AiGenerationResult.fallback("Fallback from groq")
        );
        when(fallbackAiProvider.generate("Hello")).thenReturn(
                AiGenerationResult.fallback("Deterministic fallback")
        );

        AiGenerationResult result = router.generate("Hello");

        assertNotNull(result);
        assertTrue(result.isFallback());
        verify(groqAiProvider, times(1)).generate("Hello");
        verify(geminiAiProvider, never()).generate(anyString());
        verify(geminiAiProvider, never()).generateDirect(anyString());
        verify(fallbackAiProvider, times(1)).generate("Hello");
    }

    // TEST 8 — Auto mode + Gemini success
    @Test
    @DisplayName("TEST 8: Auto mode with Gemini success makes exactly 1 cloud attempt")
    void test8_autoModeOnlyGemini() {
        when(aiConfig.getProvider()).thenReturn("auto");
        when(geminiAiProvider.isAvailable()).thenReturn(true);
        when(geminiAiProvider.generateDirect("Hello")).thenReturn(
                AiGenerationResult.success("Success text", "gemini", "gemini-3.6-flash")
        );

        AiGenerationResult result = router.generate("Hello");

        assertNotNull(result);
        assertEquals("gemini", result.provider());
        verify(geminiAiProvider, times(1)).generateDirect("Hello");
        verifyNoInteractions(groqAiProvider);
        verifyNoInteractions(fallbackAiProvider);
    }

    // TEST 9 — Auto mode + Gemini failure + Groq success
    @Test
    @DisplayName("TEST 9: Auto mode with Gemini failure + Groq success calls Gemini once and Groq once")
    void test9_autoModeGeminiFailGroqSuccess() {
        when(aiConfig.getProvider()).thenReturn("auto");
        when(geminiAiProvider.isAvailable()).thenReturn(true);
        when(geminiAiProvider.generateDirect("Hello")).thenReturn(null);
        when(groqAiProvider.isAvailable()).thenReturn(true);
        when(groqAiProvider.generateDirect("Hello")).thenReturn(
                AiGenerationResult.success("Groq text", "groq", "openai/gpt-oss-120b")
        );

        AiGenerationResult result = router.generate("Hello");

        assertNotNull(result);
        assertEquals("groq", result.provider());
        verify(geminiAiProvider, times(1)).generateDirect("Hello");
        verify(groqAiProvider, times(1)).generateDirect("Hello");
        verify(fallbackAiProvider, never()).generate(anyString());
    }

    // TEST 10 — Auto mode + both providers unavailable
    @Test
    @DisplayName("TEST 10: Auto mode with both providers unavailable immediately uses deterministic fallback")
    void test10_autoBothUnavailable() {
        when(aiConfig.getProvider()).thenReturn("auto");
        when(geminiAiProvider.isAvailable()).thenReturn(false);
        when(groqAiProvider.isAvailable()).thenReturn(false);
        when(fallbackAiProvider.generate("Hello")).thenReturn(
                AiGenerationResult.fallback("Deterministic fallback")
        );

        AiGenerationResult result = router.generate("Hello");

        assertNotNull(result);
        assertTrue(result.isFallback());
        verify(geminiAiProvider, never()).generateDirect(anyString());
        verify(groqAiProvider, never()).generateDirect(anyString());
        verify(fallbackAiProvider, times(1)).generate("Hello");
    }

    // TEST 11 — Missing Groq key
    @Test
    @DisplayName("TEST 11: Missing Groq key in auto mode falls back cleanly without crash")
    void test11_missingGroqKey() {
        when(aiConfig.getProvider()).thenReturn("auto");
        when(geminiAiProvider.isAvailable()).thenReturn(true);
        when(geminiAiProvider.generateDirect("Hello")).thenReturn(null);
        when(groqAiProvider.isAvailable()).thenReturn(false); // Key missing
        when(fallbackAiProvider.generate("Hello")).thenReturn(
                AiGenerationResult.fallback("Fallback text")
        );

        AiGenerationResult result = router.generate("Hello");

        assertNotNull(result);
        assertTrue(result.isFallback());
        verify(geminiAiProvider, times(1)).generateDirect("Hello");
        verify(groqAiProvider, never()).generateDirect(anyString());
        verify(fallbackAiProvider, times(1)).generate("Hello");
    }

    // TEST 12 — Missing Gemini key + Groq available
    @Test
    @DisplayName("TEST 12: Missing Gemini key in auto mode skips Gemini and calls Groq directly")
    void test12_missingGeminiKey_groqAvailable() {
        when(aiConfig.getProvider()).thenReturn("auto");
        when(geminiAiProvider.isAvailable()).thenReturn(false); // Key missing
        when(groqAiProvider.isAvailable()).thenReturn(true);
        when(groqAiProvider.generateDirect("Hello")).thenReturn(
                AiGenerationResult.success("Groq text", "groq", "openai/gpt-oss-120b")
        );

        AiGenerationResult result = router.generate("Hello");

        assertNotNull(result);
        assertEquals("groq", result.provider());
        assertEquals("openai/gpt-oss-120b", result.model());
        assertFalse(result.isFallback());
        verify(geminiAiProvider, never()).generateDirect(anyString());
        verify(groqAiProvider, times(1)).generateDirect("Hello");
    }

    // TEST 13 — No API keys
    @Test
    @DisplayName("TEST 13: When no API keys are configured, fallback provider is returned")
    void test13_noApiKeys() {
        when(aiConfig.getProvider()).thenReturn("fallback");
        when(fallbackAiProvider.generate("Hello")).thenReturn(
                AiGenerationResult.fallback("Offline fallback")
        );

        AiGenerationResult result = router.generate("Hello");

        assertNotNull(result);
        assertTrue(result.isFallback());
        assertEquals("fallback", result.provider());
        verifyNoInteractions(geminiAiProvider);
        verifyNoInteractions(groqAiProvider);
        verify(fallbackAiProvider, times(1)).generate("Hello");
    }

    // TEST 14 — Metadata
    @Test
    @DisplayName("TEST 14: Validates model, provider, and fallback metadata on all paths")
    void test14_metadataVerification() {
        // Gemini metadata
        AiGenerationResult gemini = AiGenerationResult.success("text", "gemini", "gemini-3.6-flash");
        assertEquals("gemini", gemini.provider());
        assertEquals("gemini-3.6-flash", gemini.model());
        assertFalse(gemini.isFallback());

        // Groq metadata
        AiGenerationResult groq = AiGenerationResult.success("text", "groq", "openai/gpt-oss-120b");
        assertEquals("groq", groq.provider());
        assertEquals("openai/gpt-oss-120b", groq.model());
        assertFalse(groq.isFallback());

        // Fallback metadata
        AiGenerationResult fallback = AiGenerationResult.fallback("text");
        assertEquals("fallback", fallback.provider());
        assertNull(fallback.model());
        assertTrue(fallback.isFallback());
    }
}
