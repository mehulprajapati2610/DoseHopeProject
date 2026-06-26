package com.dosehope.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response returned after successful login/register.
 * Frontend's auth.js expects { ok, user: {...}, token } on success,
 * or { ok: false, error } on failure — handled by GlobalExceptionHandler
 * for the failure case, this DTO covers the success shape.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;

    private UserResponse user;
}
