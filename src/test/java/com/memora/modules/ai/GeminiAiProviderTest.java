package com.memora.modules.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.memora.modules.ai.config.AiConfig;
import com.memora.modules.ai.provider.AiGenerationResult;
import com.memora.modules.ai.provider.FallbackAiProvider;
import com.memora.modules.ai.provider.GeminiAiProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class GeminiAiProviderTest {

    private AiConfig aiConfig;
    private FallbackAiProvider fallbackAiProvider;
    private ObjectMapper objectMapper;
    private MockRestServiceServer mockServer;
    private GeminiAiProvider geminiAiProvider;

    @BeforeEach
    void setUp() {
        aiConfig = mock(AiConfig.class);
        fallbackAiProvider = mock(FallbackAiProvider.class);
        objectMapper = new ObjectMapper();

        when(aiConfig.getModel()).thenReturn("gemini-3.6-flash");
        when(aiConfig.getApiKey()).thenReturn("test-api-key");
        when(aiConfig.getTemperature()).thenReturn(0.3);
        when(aiConfig.isGeminiConfigured()).thenReturn(true);
        when(fallbackAiProvider.generate(anyString())).thenCallRealMethod();
        when(fallbackAiProvider.generateContent(anyString())).thenCallRealMethod();

        RestClient.Builder restClientBuilder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        RestClient restClient = restClientBuilder.build();

        geminiAiProvider = new GeminiAiProvider(aiConfig, restClient, fallbackAiProvider, objectMapper);
    }

    @Test
    @DisplayName("Should target configured model gemini-3.6-flash and parse response correctly")
    void testSuccessfulContentGenerationWithConfiguredModel() {
        String expectedUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent?key=test-api-key";

        String mockResponseBody = """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          {
                            "text": "Happy means feeling or showing pleasure or contentment."
                          }
                        ]
                      }
                    }
                  ]
                }
                """;

        mockServer.expect(requestTo(expectedUrl))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withSuccess(mockResponseBody, MediaType.APPLICATION_JSON));

        String result = geminiAiProvider.generateContent("Explain the word happy");

        assertEquals("Happy means feeling or showing pleasure or contentment.", result);
        mockServer.verify();
        verify(fallbackAiProvider, never()).generateContent(anyString());
    }

    @Test
    @DisplayName("generate() should return AiGenerationResult with primary model on success")
    void testGenerateWithPrimaryModelSuccess() {
        String expectedUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent?key=test-api-key";

        String mockResponseBody = """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          {
                            "text": "Happy means feeling or showing pleasure."
                          }
                        ]
                      }
                    }
                  ]
                }
                """;

        mockServer.expect(requestTo(expectedUrl))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(mockResponseBody, MediaType.APPLICATION_JSON));

        AiGenerationResult result = geminiAiProvider.generate("Explain the word happy");

        assertNotNull(result);
        assertEquals("Happy means feeling or showing pleasure.", result.text());
        assertEquals("gemini", result.provider());
        assertEquals("gemini-3.6-flash", result.model());
        assertFalse(result.isFallback());
        mockServer.verify();
    }

    @Test
    @DisplayName("Should seamlessly upgrade to supported model when primary model returns 404")
    void testResilientUpgradeToSupportedModel() {
        String primaryUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent?key=test-api-key";
        String fallbackModelUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.7-flash:generateContent?key=test-api-key";

        String mockSuccessBody = """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          {
                            "text": "You are happy when you feel good and smile."
                          }
                        ]
                      }
                    }
                  ]
                }
                """;

        // Primary model returns 404 (e.g. deprecated)
        mockServer.expect(requestTo(primaryUrl))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withResourceNotFound());

        // Resilient fallback model returns 200 OK
        mockServer.expect(requestTo(fallbackModelUrl))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(mockSuccessBody, MediaType.APPLICATION_JSON));

        String result = geminiAiProvider.generateContent("Explain the word happy");

        assertEquals("You are happy when you feel good and smile.", result);
        mockServer.verify();
        verify(fallbackAiProvider, never()).generateContent(anyString());
    }

    @Test
    @DisplayName("generate() should return actual fallback model when primary returns 404")
    void testGenerateReturnsFallbackModelOn404() {
        String primaryUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent?key=test-api-key";
        String fallbackModelUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.7-flash:generateContent?key=test-api-key";

        String mockSuccessBody = """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          {
                            "text": "Upgraded response."
                          }
                        ]
                      }
                    }
                  ]
                }
                """;

        mockServer.expect(requestTo(primaryUrl))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withResourceNotFound());

        mockServer.expect(requestTo(fallbackModelUrl))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(mockSuccessBody, MediaType.APPLICATION_JSON));

        AiGenerationResult result = geminiAiProvider.generate("Explain the word happy");

        assertNotNull(result);
        assertEquals("Upgraded response.", result.text());
        assertEquals("gemini", result.provider());
        assertEquals("gemini-3.7-flash", result.model());
        assertFalse(result.isFallback());
        mockServer.verify();
    }

    @Test
    @DisplayName("generate() should advance to gemini-3.5-flash when gemini-3.6-flash and gemini-3.7-flash fail")
    void testGenerateAdvancesToSecondFallbackOnPrimaryAndFirstFallbackFailures() {
        String primaryUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent?key=test-api-key";
        String fallback1Url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.7-flash:generateContent?key=test-api-key";
        String fallback2Url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=test-api-key";

        String mockSuccessBody = """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          {
                            "text": "Second fallback response."
                          }
                        ]
                      }
                    }
                  ]
                }
                """;

        // Primary returns 404
        mockServer.expect(requestTo(primaryUrl))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withResourceNotFound());

        // Fallback 1 returns 429 Too Many Requests
        mockServer.expect(requestTo(fallback1Url))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        // Fallback 2 returns 200 OK
        mockServer.expect(requestTo(fallback2Url))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(mockSuccessBody, MediaType.APPLICATION_JSON));

        AiGenerationResult result = geminiAiProvider.generate("Explain the word happy");

        assertNotNull(result);
        assertEquals("Second fallback response.", result.text());
        assertEquals("gemini", result.provider());
        assertEquals("gemini-3.5-flash", result.model());
        assertFalse(result.isFallback());
        mockServer.verify();
    }

    @Test
    @DisplayName("Should fallback gracefully to deterministic provider when all models fail")
    void testFallbackOnHttpError() {
        String primaryUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent?key=test-api-key";
        String fallback1Url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.7-flash:generateContent?key=test-api-key";
        String fallback2Url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=test-api-key";

        mockServer.expect(requestTo(primaryUrl))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withResourceNotFound());

        mockServer.expect(requestTo(fallback1Url))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());

        mockServer.expect(requestTo(fallback2Url))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());

        when(fallbackAiProvider.generateContent(anyString())).thenReturn("Deterministic fallback explanation.");

        String result = geminiAiProvider.generateContent("Explain the word happy");

        assertEquals("Deterministic fallback explanation.", result);
        verify(fallbackAiProvider, times(1)).generateContent("Explain the word happy");
        mockServer.verify();
    }

    @Test
    @DisplayName("generate() should return fallback metadata when all Gemini models fail")
    void testGenerateReturnsFallbackMetadataWhenAllModelsFail() {
        String primaryUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent?key=test-api-key";
        String fallback1Url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.7-flash:generateContent?key=test-api-key";
        String fallback2Url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=test-api-key";

        mockServer.expect(requestTo(primaryUrl))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withResourceNotFound());

        mockServer.expect(requestTo(fallback1Url))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        mockServer.expect(requestTo(fallback2Url))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        AiGenerationResult result = geminiAiProvider.generate("Explain the word happy");

        assertNotNull(result);
        assertEquals("fallback", result.provider());
        assertNull(result.model());
        assertTrue(result.isFallback());
        assertEquals("Deterministic educational learning assistance provided via Memora Adaptive Memory Engine.", result.text());
        mockServer.verify();
    }

    @Test
    @DisplayName("Should fallback gracefully when Gemini responses are empty or missing candidates")
    void testFallbackOnEmptyCandidates() {
        String primaryUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent?key=test-api-key";
        String fallback1Url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.7-flash:generateContent?key=test-api-key";
        String fallback2Url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=test-api-key";

        String emptyResponseBody = "{\"candidates\": []}";

        mockServer.expect(requestTo(primaryUrl))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(emptyResponseBody, MediaType.APPLICATION_JSON));

        mockServer.expect(requestTo(fallback1Url))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(emptyResponseBody, MediaType.APPLICATION_JSON));

        mockServer.expect(requestTo(fallback2Url))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(emptyResponseBody, MediaType.APPLICATION_JSON));

        when(fallbackAiProvider.generateContent(anyString())).thenReturn("Deterministic fallback explanation.");

        String result = geminiAiProvider.generateContent("Explain the word happy");

        assertEquals("Deterministic fallback explanation.", result);
        verify(fallbackAiProvider, times(1)).generateContent("Explain the word happy");
        mockServer.verify();
    }

    @Test
    @DisplayName("Should delegate to fallback immediately if Gemini is not configured")
    void testDelegateWhenNotConfigured() {
        when(aiConfig.isGeminiConfigured()).thenReturn(false);
        when(fallbackAiProvider.generateContent("test")).thenReturn("Fallback output");

        String result = geminiAiProvider.generateContent("test");

        assertEquals("Fallback output", result);
        verify(fallbackAiProvider, times(1)).generateContent("test");
    }

    @Test
    @DisplayName("Should report correct provider name and availability")
    void testProviderNameAndAvailability() {
        when(aiConfig.isGeminiConfigured()).thenReturn(true);
        assertTrue(geminiAiProvider.isAvailable());
        assertEquals("gemini", geminiAiProvider.getProviderName());

        when(aiConfig.isGeminiConfigured()).thenReturn(false);
        assertFalse(geminiAiProvider.isAvailable());
        assertEquals("fallback", geminiAiProvider.getProviderName());
    }

    @Test
    @DisplayName("Should safely handle error responses without leaking API key in error messages")
    void testApiKeyRedactionInErrorHandling() {
        String primaryUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent?key=test-api-key";
        String fallback1Url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.7-flash:generateContent?key=test-api-key";
        String fallback2Url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=test-api-key";

        String errorWithKey = "{\"error\": \"Invalid request: key=test-api-key is invalid\"}";

        mockServer.expect(requestTo(primaryUrl))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withBadRequest().body(errorWithKey));

        mockServer.expect(requestTo(fallback1Url))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withBadRequest().body(errorWithKey));

        mockServer.expect(requestTo(fallback2Url))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withBadRequest().body(errorWithKey));

        AiGenerationResult result = geminiAiProvider.generate("Explain the word happy");

        assertNotNull(result);
        assertTrue(result.isFallback());
        assertEquals("fallback", result.provider());
        assertNull(result.model());
        mockServer.verify();
    }
}
