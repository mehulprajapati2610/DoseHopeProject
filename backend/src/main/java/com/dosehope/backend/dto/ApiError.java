package com.dosehope.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard error shape returned by GlobalExceptionHandler.
 * Frontend's data-store.js consistently checks result.ok and result.error
 * (e.g. registerUser, loginUser, addMedicine all return {ok:false, error}).
 * This DTO gives the same { error, status, timestamp } shape for every
 * failure so the frontend can be updated to read error.message uniformly.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiError {

    private int status;
    private String error;
    private String message;
    private LocalDateTime timestamp;
}
