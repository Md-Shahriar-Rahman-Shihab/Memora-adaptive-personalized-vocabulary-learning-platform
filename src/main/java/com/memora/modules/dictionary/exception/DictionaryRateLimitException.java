package com.memora.modules.dictionary.exception;

import com.memora.common.exception.ApiException;
import org.springframework.http.HttpStatus;

/**
 * Thrown when Merriam-Webster API queries exceed provider rate limits (HTTP 429).
 */
public class DictionaryRateLimitException extends ApiException {

    public DictionaryRateLimitException() {
        super("Dictionary service rate limit exceeded. Please try again later.", HttpStatus.TOO_MANY_REQUESTS);
    }

    public DictionaryRateLimitException(String message) {
        super(message, HttpStatus.TOO_MANY_REQUESTS);
    }
}
