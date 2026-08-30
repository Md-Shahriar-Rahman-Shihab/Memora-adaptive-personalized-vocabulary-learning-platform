package com.memora.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a request is malformed or violates business validation rules.
 */
public class BadRequestException extends ApiException {

    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
