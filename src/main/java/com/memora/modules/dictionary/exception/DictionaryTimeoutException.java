package com.memora.modules.dictionary.exception;

import com.memora.common.exception.ApiException;
import org.springframework.http.HttpStatus;

/**
 * Thrown when external dictionary service requests exceed configured connect or read timeouts.
 */
public class DictionaryTimeoutException extends ApiException {

    public DictionaryTimeoutException() {
        super("Dictionary service took too long to respond. Please try again.", HttpStatus.GATEWAY_TIMEOUT);
    }

    public DictionaryTimeoutException(String message) {
        super(message, HttpStatus.GATEWAY_TIMEOUT);
    }

    public DictionaryTimeoutException(String message, Throwable cause) {
        super(message, cause, HttpStatus.GATEWAY_TIMEOUT);
    }
}
