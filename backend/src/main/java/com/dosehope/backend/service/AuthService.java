package com.dosehope.backend.service;

import com.dosehope.backend.dto.AuthResponse;
import com.dosehope.backend.dto.LoginRequest;
import com.dosehope.backend.dto.RegisterRequest;
import com.dosehope.backend.dto.UserResponse;
import com.dosehope.backend.entity.User;
import com.dosehope.backend.exception.DuplicateResourceException;
import com.dosehope.backend.exception.InvalidCredentialsException;
import com.dosehope.backend.repository.UserRepository;
import com.dosehope.backend.security.JwtUtil;
import com.dosehope.backend.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Mirrors frontend's data-store.js registerUser() / loginUser().
 * Frontend stored plain-text passwords in localStorage (acceptable only
 * because it was a disposable mock); here passwords are BCrypt-hashed
 * before storage, never returned in any response.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            // Same message frontend's registerUser() returns on duplicate email.
            throw new DuplicateResourceException("An account with this email already exists.");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .phone(request.getPhone())
                .address(request.getAddress())
                .build();

        User saved = userRepository.save(user);

        String token = jwtUtil.generateToken(new UserPrincipal(saved));

        return AuthResponse.builder()
                .token(token)
                .user(UserResponse.fromEntity(saved))
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                // Same message frontend's loginUser() returns on failure —
                // deliberately vague so callers can't enumerate which part was wrong.
                .orElseThrow(() -> new InvalidCredentialsException("Incorrect email or password."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Incorrect email or password.");
        }

        String token = jwtUtil.generateToken(new UserPrincipal(user));

        return AuthResponse.builder()
                .token(token)
                .user(UserResponse.fromEntity(user))
                .build();
    }
}
