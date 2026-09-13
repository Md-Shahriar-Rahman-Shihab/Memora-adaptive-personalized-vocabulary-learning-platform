package com.memora.modules.dictionary.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Configuration for the external Merriam-Webster Collegiate Dictionary API client,
 * managing connection properties, timeout settings, and reusable HTTP client beans.
 */
@Configuration
public class DictionaryConfig {

    @Value("${memora.dictionary.api-url:${MEMORA_MERRIAM_WEBSTER_API_URL:https://www.dictionaryapi.com/api/v3/references/collegiate/json}}")
    private String apiUrl;

    @Value("${memora.dictionary.api-key:${MEMORA_MERRIAM_WEBSTER_API_KEY:}}")
    private String apiKey;

    @Value("${memora.dictionary.connect-timeout-ms:${MEMORA_DICTIONARY_CONNECT_TIMEOUT_MS:5000}}")
    private int connectTimeoutMs;

    @Value("${memora.dictionary.read-timeout-ms:${MEMORA_DICTIONARY_READ_TIMEOUT_MS:10000}}")
    private int readTimeoutMs;

    public String getApiUrl() {
        return apiUrl != null && !apiUrl.isBlank()
                ? apiUrl.trim()
                : "https://www.dictionaryapi.com/api/v3/references/collegiate/json";
    }

    public String getApiKey() {
        return apiKey != null ? apiKey.trim() : "";
    }

    public boolean isApiKeyConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs > 0 ? connectTimeoutMs : 5000;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs > 0 ? readTimeoutMs : 10000;
    }

    @Bean(name = "dictionaryHttpClient")
    public HttpClient dictionaryHttpClient() {
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofMillis(getConnectTimeoutMs()))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @Bean(name = "dictionaryRestClient")
    public RestClient dictionaryRestClient(HttpClient dictionaryHttpClient) {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(dictionaryHttpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(getReadTimeoutMs()));

        return RestClient.builder()
                .requestFactory(requestFactory)
                .defaultHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Memora/1.0")
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
