package com.dosehope.backend.exception;

/**
 * Thrown when a requested entity (User, Medicine, DonationRequest) does not
 * exist. Mapped to HTTP 404 in GlobalExceptionHandler.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
