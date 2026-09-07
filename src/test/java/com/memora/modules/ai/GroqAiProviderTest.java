package com.memora.modules.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.memora.modules.ai.config.AiConfig;
import com.memora.modules.ai.provider.AiGenerationResult;
import com.memora.modules.ai.provider.FallbackAiProvider;
import com.memora.modules.ai.provider.GroqAiProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class GroqAiProviderTest {

    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";

    private AiConfig aiConfig;
    private FallbackAiProvider fallbackAiProvider;
    private ObjectMapper objectMapper;
    private MockRestServiceServer mockServer;
    private GroqAiProvider groqAiProvider;

    @BeforeEach
    void setUp() {
        aiConfig = mock(AiConfig.class);
        fallbackAiProvider = mock(FallbackAiProvider.class);
        objectMapper = new ObjectMapper();

        when(aiConfig.getGroqModel()).thenReturn("openai/gpt-oss-120b");
        when(aiConfig.getGroqApiKey()).thenReturn("gsk_test_mock_key_12345");
        when(aiConfig.getTemperature()).thenReturn(0.3);
        when(aiConfig.isGroqConfigured()).thenReturn(true);
        when(fallbackAiProvider.generate(anyString())).thenCallRealMethod();
        when(fallbackAiProvider.generateContent(anyString())).thenCallRealMethod();

        RestClient.Builder restClientBuilder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        RestClient restClient = restClientBuilder.build();

        groqAiProvider = new GroqAiProvider(aiConfig, restClient, fallbackAiProvider, objectMapper);
    }

    @Test
    @DisplayName("Should target Groq chat completions API with model openai/gpt-oss-120b and parse content")
    void testSuccessfulGeneration() {
        String mockResponseBody = """
                {
                  "id": "chatcmpl-test-123",
                  "object": "chat.completion",
                  "created": 1700000000,
                  "model": "openai/gpt-oss-120b",
                  "choices": [
                    {
                      "index": 0,
                      "message": {
                        "role": "assistant",
                        "content": "Resilient describes the capacity to recover quickly from difficulties."
                      },
                      "finish_reason": "stop"
                    }
                  ]
                }
                """;

        mockServer.expect(requestTo(GROQ_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer gsk_test_mock_key_12345"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.model").value("openai/gpt-oss-120b"))
                .andExpect(jsonPath("$.messages[0].content").value("Explain resilient"))
                .andRespond(withSuccess(mockResponseBody, MediaType.APPLICATION_JSON));

        AiGenerationResult result = groqAiProvider.generate("Explain resilient");

        mockServer.verify();
        assertNotNull(result);
        assertEquals("Resilient describes the capacity to recover quickly from difficulties.", result.text());
        assertEquals("groq", result.provider());
        assertEquals("openai/gpt-oss-120b", result.model());
        assertFalse(result.isFallback());
    }

    @Test
    @DisplayName("Should handle HTTP 429 rate limit safely and delegate to fallback")
    void testRateLimit429Handling() {
        mockServer.expect(requestTo(GROQ_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS)
                        .body("{\"error\": {\"message\": \"Rate limit exceeded\"}}")
                        .contentType(MediaType.APPLICATION_JSON));

        AiGenerationResult result = groqAiProvider.generate("Explain resilient");

        mockServer.verify();
        assertNotNull(result);
        assertTrue(result.isFallback());
        assertEquals("fallback", result.provider());
        assertNull(result.model());
    }

    @Test
    @DisplayName("generateDirect returns null on HTTP 429 so router can take control")
    void testGenerateDirectOn429() {
        mockServer.expect(requestTo(GROQ_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS)
                        .body("{\"error\": {\"message\": \"Rate limit exceeded\"}}")
                        .contentType(MediaType.APPLICATION_JSON));

        AiGenerationResult result = groqAiProvider.generateDirect("Explain resilient");

        mockServer.verify();
        assertNull(result);
    }

    @Test
    @DisplayName("Should handle HTTP 500 server error safely and return fallback")
    void testHttp500Handling() {
        mockServer.expect(requestTo(GROQ_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError()
                        .body("{\"error\": {\"message\": \"Internal Groq Error\"}}")
                        .contentType(MediaType.APPLICATION_JSON));

        AiGenerationResult result = groqAiProvider.generate("Explain resilient");

        mockServer.verify();
        assertNotNull(result);
        assertTrue(result.isFallback());
    }

    @Test
    @DisplayName("Should handle malformed JSON safely and return fallback")
    void testMalformedJsonHandling() {
        mockServer.expect(requestTo(GROQ_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("Not a valid json response", MediaType.APPLICATION_JSON));

        AiGenerationResult result = groqAiProvider.generate("Explain resilient");

        mockServer.verify();
        assertNotNull(result);
        assertTrue(result.isFallback());
    }

    @Test
    @DisplayName("Should immediately return fallback when Groq is not configured")
    void testNotConfigured() {
        when(aiConfig.isGroqConfigured()).thenReturn(false);

        AiGenerationResult result = groqAiProvider.generate("Explain resilient");

        assertNotNull(result);
        assertTrue(result.isFallback());
        // Verify no network request was made
        mockServer.verify();
    }
}
