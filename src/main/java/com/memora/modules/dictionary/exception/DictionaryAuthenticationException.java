package com.memora.modules.dictionary.exception;

import com.memora.common.exception.ApiException;
import org.springframework.http.HttpStatus;

/**
 * Thrown when Merriam-Webster API key is invalid, missing, or rejected (HTTP 401/403).
 */
public class DictionaryAuthenticationException extends ApiException {

    public DictionaryAuthenticationException() {
        super("Dictionary service is not configured or authentication failed. Please check your Merriam-Webster API key.", HttpStatus.SERVICE_UNAVAILABLE);
    }

    public DictionaryAuthenticationException(String message) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE);
    }
}
