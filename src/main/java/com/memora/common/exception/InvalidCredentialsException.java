package com.memora.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when authentication fails due to incorrect credentials.
 */
public class InvalidCredentialsException extends ApiException {

    public InvalidCredentialsException() {
        super("Invalid email or password", HttpStatus.UNAUTHORIZED);
    }

    public InvalidCredentialsException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
