package com.memora.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when attempting to register with an email that is already in use.
 */
public class EmailAlreadyExistsException extends ApiException {

    public EmailAlreadyExistsException(String email) {
        super(String.format("User with email '%s' already exists", email), HttpStatus.CONFLICT);
    }
}
