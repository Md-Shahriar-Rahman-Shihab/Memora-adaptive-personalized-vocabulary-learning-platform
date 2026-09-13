package com.memora.modules.dictionary.exception;

import com.memora.common.exception.ApiException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when the external dictionary API is unavailable, times out, or fails to respond.
 */
public class DictionaryServiceUnavailableException extends ApiException {

    public DictionaryServiceUnavailableException() {
        super("Dictionary service is temporarily unavailable. Please try again.", HttpStatus.SERVICE_UNAVAILABLE);
    }

    public DictionaryServiceUnavailableException(String message) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE);
    }

    public DictionaryServiceUnavailableException(String message, Throwable cause) {
        super(message, cause, HttpStatus.SERVICE_UNAVAILABLE);
    }
}
