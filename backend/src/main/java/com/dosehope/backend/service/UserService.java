package com.dosehope.backend.service;

import com.dosehope.backend.dto.UserResponse;
import com.dosehope.backend.entity.Role;
import com.dosehope.backend.entity.User;
import com.dosehope.backend.exception.ResourceNotFoundException;
import com.dosehope.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Mirrors frontend's data-store.js getAllVolunteers() and the implicit
 * getCurrentUser()-by-id lookups used across ngo-dashboard.js / browse.js.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return UserResponse.fromEntity(user);
    }

    /**
     * Mirrors frontend's getAllVolunteers() — used to populate the NGO
     * dashboard's "Assign Volunteer" dropdown.
     */
    public List<UserResponse> getAllVolunteers() {
        return userRepository.findByRole(Role.VOLUNTEER).stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    public List<UserResponse> getAllNgos() {
        return userRepository.findByRole(Role.NGO).stream()
                .map(UserResponse::fromEntity)
                .toList();
    }
}
