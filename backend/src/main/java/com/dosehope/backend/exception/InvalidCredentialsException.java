package com.dosehope.backend.exception;

/**
 * Thrown on failed login — wrong email or password.
 * Mirrors frontend's loginUser() error: "Incorrect email or password."
 * Mapped to HTTP 401 in GlobalExceptionHandler.
 */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
