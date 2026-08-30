package com.memora.common.exception;

/**
 * Exception thrown when a user is not found in the system.
 */
public class UserNotFoundException extends ResourceNotFoundException {

    public UserNotFoundException(String email) {
        super("User", "email", email);
    }

    public UserNotFoundException(Long id) {
        super("User", "id", id);
    }
}
