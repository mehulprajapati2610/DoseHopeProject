package com.dosehope.backend.exception;

/**
 * Thrown when an action violates a business rule that isn't a simple
 * validation error — e.g. listing an already-expired medicine, requesting
 * a medicine that's no longer available, assigning a non-existent volunteer.
 * Mirrors frontend's addMedicine()/createRequest() error returns.
 * Mapped to HTTP 400 in GlobalExceptionHandler.
 */
public class InvalidOperationException extends RuntimeException {
    public InvalidOperationException(String message) {
        super(message);
    }
}
