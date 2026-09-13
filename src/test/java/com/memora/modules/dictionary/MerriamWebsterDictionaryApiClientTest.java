package com.memora.modules.dictionary;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.memora.modules.dictionary.client.MerriamWebsterDictionaryApiClient;
import com.memora.modules.dictionary.config.DictionaryConfig;
import com.memora.modules.dictionary.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class MerriamWebsterDictionaryApiClientTest {

    private DictionaryConfig dictionaryConfig;
    private ObjectMapper objectMapper;
    private MockRestServiceServer mockServer;
    private MerriamWebsterDictionaryApiClient apiClient;

    private static final String TEST_API_KEY = "test-secret-mw-key";
    private static final String BASE_URL = "https://www.dictionaryapi.com/api/v3/references/collegiate/json";

    @BeforeEach
    void setUp() {
        dictionaryConfig = mock(DictionaryConfig.class);
        when(dictionaryConfig.getApiUrl()).thenReturn(BASE_URL);
        when(dictionaryConfig.getApiKey()).thenReturn(TEST_API_KEY);
        when(dictionaryConfig.isApiKeyConfigured()).thenReturn(true);

        objectMapper = new ObjectMapper();

        RestClient.Builder restClientBuilder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        RestClient restClient = restClientBuilder.build();

        apiClient = new MerriamWebsterDictionaryApiClient(restClient, dictionaryConfig, objectMapper);
    }

    @Test
    @DisplayName("Should successfully fetch word entries from Merriam-Webster API")
    void testSuccessfulFetch() {
        String expectedUrl = BASE_URL + "/happy?key=" + TEST_API_KEY;
        String mockResponse = """
                [
                  {
                    "meta": { "id": "happy:1" },
                    "hwi": { "hw": "hap*py" },
                    "fl": "adjective",
                    "shortdef": ["fortunate", "glad"]
                  }
                ]
                """;

        mockServer.expect(requestTo(expectedUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mockResponse, MediaType.APPLICATION_JSON));

        JsonNode result = apiClient.fetchWordEntries("happy");

        mockServer.verify();
        assertNotNull(result);
        assertTrue(result.isArray());
        assertEquals(1, result.size());
        assertEquals("happy:1", result.get(0).get("meta").get("id").asText());
    }

    @Test
    @DisplayName("Should return spelling suggestions array when word is misspelled")
    void testSpellingSuggestionsFetch() {
        String expectedUrl = BASE_URL + "/hapy?key=" + TEST_API_KEY;
        String mockResponse = "[\"happy\",\"happier\",\"happily\",\"haply\"]";

        mockServer.expect(requestTo(expectedUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mockResponse, MediaType.APPLICATION_JSON));

        JsonNode result = apiClient.fetchWordEntries("hapy");

        mockServer.verify();
        assertNotNull(result);
        assertTrue(result.isArray());
        assertTrue(result.get(0).isTextual());
        assertEquals("happy", result.get(0).asText());
    }

    @Test
    @DisplayName("Should throw DictionaryAuthenticationException when API key is missing or not configured")
    void testMissingApiKeyThrowsAuthException() {
        when(dictionaryConfig.isApiKeyConfigured()).thenReturn(false);

        assertThrows(DictionaryAuthenticationException.class, () -> apiClient.fetchWordEntries("happy"));
    }

    @Test
    @DisplayName("Should throw DictionaryAuthenticationException on HTTP 401 Unauthorized")
    void testUnauthorizedThrowsException() {
        String expectedUrl = BASE_URL + "/unauthorized?key=" + TEST_API_KEY;

        mockServer.expect(requestTo(expectedUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        assertThrows(DictionaryAuthenticationException.class, () -> apiClient.fetchWordEntries("unauthorized"));
        mockServer.verify();
    }

    @Test
    @DisplayName("Should throw DictionaryAuthenticationException on HTTP 403 Forbidden")
    void testForbiddenThrowsException() {
        String expectedUrl = BASE_URL + "/forbidden?key=" + TEST_API_KEY;

        mockServer.expect(requestTo(expectedUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.FORBIDDEN));

        assertThrows(DictionaryAuthenticationException.class, () -> apiClient.fetchWordEntries("forbidden"));
        mockServer.verify();
    }

    @Test
    @DisplayName("Should throw DictionaryAuthenticationException when provider body contains 'Invalid API key'")
    void testInvalidKeyBodyThrowsException() {
        String expectedUrl = BASE_URL + "/test?key=" + TEST_API_KEY;

        mockServer.expect(requestTo(expectedUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("Invalid API key", MediaType.TEXT_PLAIN));

        assertThrows(DictionaryAuthenticationException.class, () -> apiClient.fetchWordEntries("test"));
        mockServer.verify();
    }

    @Test
    @DisplayName("Should throw DictionaryRateLimitException on HTTP 429 Too Many Requests")
    void testRateLimitThrowsException() {
        String expectedUrl = BASE_URL + "/ratelimited?key=" + TEST_API_KEY;

        mockServer.expect(requestTo(expectedUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        assertThrows(DictionaryRateLimitException.class, () -> apiClient.fetchWordEntries("ratelimited"));
        mockServer.verify();
    }

    @Test
    @DisplayName("Should throw DictionaryWordNotFoundException on HTTP 404 response")
    void testNotFoundThrowsException() {
        String expectedUrl = BASE_URL + "/unknown?key=" + TEST_API_KEY;

        mockServer.expect(requestTo(expectedUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThrows(DictionaryWordNotFoundException.class, () -> apiClient.fetchWordEntries("unknown"));
        mockServer.verify();
    }

    @Test
    @DisplayName("Should throw DictionaryServiceUnavailableException on HTTP 500 Server Error")
    void testServerErrorThrowsException() {
        String expectedUrl = BASE_URL + "/error?key=" + TEST_API_KEY;

        mockServer.expect(requestTo(expectedUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        assertThrows(DictionaryServiceUnavailableException.class, () -> apiClient.fetchWordEntries("error"));
        mockServer.verify();
    }

    @Test
    @DisplayName("Should safely URL-encode words containing special characters or hyphens")
    void testUrlEncoding() {
        String expectedUrl = BASE_URL + "/mother-in-law?key=" + TEST_API_KEY;

        mockServer.expect(requestTo(expectedUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        JsonNode result = apiClient.fetchWordEntries("mother-in-law");

        mockServer.verify();
        assertNotNull(result);
        assertTrue(result.isArray());
        assertEquals(0, result.size());
    }

}
