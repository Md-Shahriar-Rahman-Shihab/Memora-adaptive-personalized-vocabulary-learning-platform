package com.memora.modules.dictionary.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.memora.modules.dictionary.config.DictionaryConfig;
import com.memora.modules.dictionary.exception.DictionaryAuthenticationException;
import com.memora.modules.dictionary.exception.DictionaryRateLimitException;
import com.memora.modules.dictionary.exception.DictionaryServiceUnavailableException;
import com.memora.modules.dictionary.exception.DictionaryTimeoutException;
import com.memora.modules.dictionary.exception.DictionaryWordNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * HTTP client communicating with the official Merriam-Webster Collegiate Dictionary API using Spring's RestClient.
 * Provides safe URI construction, timeout handling, and diagnostic logging without exposing secret API keys.
 */
@Component
public class MerriamWebsterDictionaryApiClient implements DictionaryProvider {

    private static final Logger log = LoggerFactory.getLogger(MerriamWebsterDictionaryApiClient.class);

    private final RestClient restClient;
    private final DictionaryConfig dictionaryConfig;
    private final ObjectMapper objectMapper;

    public MerriamWebsterDictionaryApiClient(
            @Qualifier("dictionaryRestClient") RestClient restClient,
            DictionaryConfig dictionaryConfig,
            ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.dictionaryConfig = dictionaryConfig;
        this.objectMapper = objectMapper;
    }

    /**
     * Queries the Merriam-Webster Collegiate Dictionary API for the requested word.
     *
     * @param word Normalized, sanitized word
     * @return Root JsonNode containing either entry objects or spelling suggestions
     * @throws DictionaryWordNotFoundException if word is not found and no suggestions exist
     * @throws DictionaryAuthenticationException if API key is invalid or unauthorized
     * @throws DictionaryRateLimitException if provider rate limits are exceeded
     * @throws DictionaryTimeoutException if network/socket timeout occurs
     * @throws DictionaryServiceUnavailableException if upstream service is unreachable
     */
    @Override
    public JsonNode fetchWordEntries(String word) {
        if (!dictionaryConfig.isApiKeyConfigured()) {
            log.warn("Merriam-Webster API key is not configured in MEMORA_MERRIAM_WEBSTER_API_KEY");
            throw new DictionaryAuthenticationException(
                    "Dictionary service is not configured. Please provide a valid Merriam-Webster API key."
            );
        }

        log.info("Dictionary request started: word={}", word);
        long startTime = System.currentTimeMillis();

        String encodedWord = URLEncoder.encode(word, StandardCharsets.UTF_8).replace("+", "%20");
        String uriString = dictionaryConfig.getApiUrl() + "/" + encodedWord + "?key=" + dictionaryConfig.getApiKey();

        try {
            String responseBody = restClient.get()
                    .uri(URI.create(uriString))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .onStatus(status -> status.value() == 401 || status.value() == 403, (request, response) -> {
                        log.error("Dictionary request failed: word={}, reason=invalid API key ({})", word, response.getStatusCode());
                        throw new DictionaryAuthenticationException();
                    })
                    .onStatus(status -> status.value() == 404, (request, response) -> {
                        log.info("Dictionary request failed: word={}, reason=not found (404)", word);
                        throw new DictionaryWordNotFoundException(word);
                    })
                    .onStatus(status -> status.value() == 429, (request, response) -> {
                        log.warn("Dictionary request failed: word={}, reason=rate limited (429)", word);
                        throw new DictionaryRateLimitException();
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                        log.error("Dictionary request failed: word={}, reason=provider 5xx ({})", word, response.getStatusCode());
                        throw new DictionaryServiceUnavailableException();
                    })
                    .body(String.class);

            long elapsed = System.currentTimeMillis() - startTime;

            if (responseBody == null || responseBody.isBlank()) {
                log.info("Dictionary request completed: word={}, status=200 (empty response) in {}ms", word, elapsed);
                throw new DictionaryWordNotFoundException(word);
            }

            // Check if provider returned raw string error like "Invalid API key" or "Key is required"
            String trimmed = responseBody.trim();
            if (trimmed.equalsIgnoreCase("Invalid API key") || trimmed.contains("Invalid API key") || trimmed.equalsIgnoreCase("Key is required")) {
                log.error("Dictionary request failed: word={}, reason=provider rejected key in {}ms", word, elapsed);
                throw new DictionaryAuthenticationException();
            }

            JsonNode rootNode = objectMapper.readTree(responseBody);
            log.info("Dictionary request completed: word={}, status=200 in {}ms", word, elapsed);
            return rootNode;
        } catch (DictionaryAuthenticationException | DictionaryRateLimitException | DictionaryWordNotFoundException ex) {
            throw ex;
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden ex) {
            log.error("Dictionary request failed: word={}, reason=auth error: {}", word, ex.getStatusCode());
            throw new DictionaryAuthenticationException();
        } catch (HttpClientErrorException.NotFound ex) {
            log.info("Dictionary request failed: word={}, reason=provider 404", word);
            throw new DictionaryWordNotFoundException(word);
        } catch (HttpClientErrorException.TooManyRequests ex) {
            log.warn("Dictionary request failed: word={}, reason=rate limit 429", word);
            throw new DictionaryRateLimitException();
        } catch (ResourceAccessException ex) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error("Dictionary request failed: word={}, reason=timeout in {}ms: {}", word, elapsed, ex.getMessage());
            throw new DictionaryTimeoutException("Dictionary service took too long to respond. Please try again.", ex);
        } catch (RestClientResponseException ex) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error("Dictionary request failed: word={}, status={} in {}ms", word, ex.getStatusCode(), elapsed);
            throw new DictionaryServiceUnavailableException("Dictionary service is temporarily unavailable. Please try again.", ex);
        } catch (Exception ex) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error("Dictionary request failed: word={}, reason=unexpected error in {}ms: {}", word, elapsed, ex.getMessage(), ex);
            throw new DictionaryServiceUnavailableException("Dictionary service is temporarily unavailable. Please try again.", ex);
        }
    }
}
