package com.dosehope.backend.exception;

/**
 * Thrown when attempting to create a resource that violates a uniqueness
 * constraint — e.g. registering with an email that already exists.
 * Mirrors frontend's registerUser() check: "An account with this email
 * already exists." Mapped to HTTP 409 in GlobalExceptionHandler.
 */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
