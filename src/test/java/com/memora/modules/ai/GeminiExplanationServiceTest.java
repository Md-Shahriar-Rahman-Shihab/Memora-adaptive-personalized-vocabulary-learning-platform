package com.memora.modules.ai;

import com.memora.modules.ai.cache.AiResponseCache;
import com.memora.modules.ai.config.AiConfig;
import com.memora.modules.ai.dto.*;
import com.memora.modules.ai.provider.AiGenerationResult;
import com.memora.modules.ai.provider.GeminiAiProvider;
import com.memora.modules.ai.service.FallbackExplanationService;
import com.memora.modules.ai.service.GeminiExplanationService;
import com.memora.modules.user.repository.UserRepository;
import com.memora.modules.vocabulary.domain.DifficultyLevel;
import com.memora.modules.vocabulary.domain.WordCategory;
import com.memora.modules.vocabulary.entity.VocabularyWord;
import com.memora.modules.vocabulary.repository.UserWordProgressRepository;
import com.memora.modules.vocabulary.repository.VocabularyWordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeminiExplanationServiceTest {

    @Mock
    private AiConfig aiConfig;

    @Mock
    private GeminiAiProvider geminiAiProvider;

    @Mock
    private FallbackExplanationService fallbackService;

    @Mock
    private VocabularyWordRepository wordRepository;

    @Mock
    private UserWordProgressRepository progressRepository;

    @Mock
    private UserRepository userRepository;

    private AiResponseCache responseCache;
    private GeminiExplanationService service;
    private VocabularyWord sampleWord;

    @BeforeEach
    void setUp() {
        responseCache = new AiResponseCache();
        service = new GeminiExplanationService(
                aiConfig,
                geminiAiProvider,
                fallbackService,
                responseCache,
                wordRepository,
                progressRepository,
                userRepository
        );

        sampleWord = new VocabularyWord();
        sampleWord.setId(1L);
        sampleWord.setWord("meticulous");
        sampleWord.setMeaning("showing great attention to detail");
        sampleWord.setCategory(WordCategory.ACADEMIC);
        sampleWord.setDifficultyLevel(DifficultyLevel.B2);
    }

    @Test
    @DisplayName("Should delegate to fallback when Gemini is not configured")
    void testFallbackWhenNotConfigured() {
        when(aiConfig.isGeminiConfigured()).thenReturn(false);
        when(wordRepository.findByWordIgnoreCase("meticulous")).thenReturn(Optional.of(sampleWord));

        AiExplanationResponse fallbackResp = new AiExplanationResponse(
                "meticulous", "B2", "Fallback explanation", "Breakdown", "fallback", null, true, false
        );
        when(fallbackService.explainWord(eq("learner@example.com"), any())).thenReturn(fallbackResp);

        AiExplanationRequest request = new AiExplanationRequest(null, "meticulous", "B2");
        AiExplanationResponse response = service.explainWord("learner@example.com", request);

        assertNotNull(response);
        assertTrue(response.isFallback());
        assertEquals("fallback", response.provider());
        assertNull(response.model());
        verify(geminiAiProvider, never()).generate(anyString());
    }

    @Test
    @DisplayName("Should return Gemini response with exact model when available and configured")
    void testGeminiResponse() {
        when(aiConfig.isGeminiConfigured()).thenReturn(true);
        when(wordRepository.findByWordIgnoreCase("meticulous")).thenReturn(Optional.of(sampleWord));
        when(geminiAiProvider.generate(anyString())).thenReturn(
                AiGenerationResult.success("AI-generated nuanced explanation for meticulous.", "gemini", "gemini-3.6-flash")
        );

        AiExplanationRequest request = new AiExplanationRequest(null, "meticulous", "B2");
        AiExplanationResponse response = service.explainWord("learner@example.com", request);

        assertNotNull(response);
        assertEquals("meticulous", response.word());
        assertEquals("AI-generated nuanced explanation for meticulous.", response.explanation());
        assertEquals("gemini", response.provider());
        assertEquals("gemini-3.6-flash", response.model());
        assertFalse(response.isFallback());
        assertFalse(response.cached());
    }

    @Test
    @DisplayName("Should return cached response on second call preserving actual model")
    void testCachedResponse() {
        when(aiConfig.isGeminiConfigured()).thenReturn(true);
        when(wordRepository.findByWordIgnoreCase("meticulous")).thenReturn(Optional.of(sampleWord));
        when(geminiAiProvider.generate(anyString())).thenReturn(
                AiGenerationResult.success("AI explanation.", "gemini", "gemini-3.6-flash")
        );

        AiExplanationRequest request = new AiExplanationRequest(null, "meticulous", "B2");
        service.explainWord("learner@example.com", request);

        // Second call
        AiExplanationResponse cachedResponse = service.explainWord("learner@example.com", request);

        assertNotNull(cachedResponse);
        assertTrue(cachedResponse.cached());
        assertEquals("gemini-3.6-flash", cachedResponse.model());
        verify(geminiAiProvider, times(1)).generate(anyString());
    }

    @Test
    @DisplayName("Should gracefully delegate to fallback if provider throws exception")
    void testGracefulFallbackOnException() {
        when(aiConfig.isGeminiConfigured()).thenReturn(true);
        when(wordRepository.findByWordIgnoreCase("meticulous")).thenReturn(Optional.of(sampleWord));
        when(geminiAiProvider.generate(anyString())).thenThrow(new RuntimeException("Connection timed out"));

        AiExplanationResponse fallbackResp = new AiExplanationResponse(
                "meticulous", "B2", "Fallback explanation", "Breakdown", "fallback", null, true, false
        );
        when(fallbackService.explainWord(eq("learner@example.com"), any())).thenReturn(fallbackResp);

        AiExplanationRequest request = new AiExplanationRequest(null, "meticulous", "B2");
        AiExplanationResponse response = service.explainWord("learner@example.com", request);

        assertNotNull(response);
        assertTrue(response.isFallback());
        assertEquals("fallback", response.provider());
        assertNull(response.model());
    }

    @Test
    @DisplayName("explainUsage parses JSON output, filters invalid collocations, and sets exact model")
    void testExplainUsageWithJsonOutput() {
        when(aiConfig.isGeminiConfigured()).thenReturn(true);
        when(wordRepository.findByWordIgnoreCase("meticulous")).thenReturn(Optional.of(sampleWord));

        String json = """
                ```json
                {
                  "usageNotes": "Used to describe people or work requiring high precision.",
                  "register": "Professional",
                  "collocations": ["meticulous research", "meticulous planning", "demonstrate meticulous"]
                }
                ```
                """;

        when(geminiAiProvider.generate(anyString())).thenReturn(
                AiGenerationResult.success(json, "gemini", "gemini-3.6-flash")
        );

        AiUsageRequest request = new AiUsageRequest(null, "meticulous", "B2");
        AiUsageResponse response = service.explainUsage("learner@example.com", request);

        assertNotNull(response);
        assertEquals("meticulous", response.word());
        assertEquals("Professional", response.register());
        assertEquals("gemini", response.provider());
        assertEquals("gemini-3.6-flash", response.model());
        assertFalse(response.isFallback());
        // Verify ungrammatical collocation was filtered
        assertFalse(response.collocations().contains("demonstrate meticulous"));
        assertTrue(response.collocations().contains("meticulous research"));
    }
}
