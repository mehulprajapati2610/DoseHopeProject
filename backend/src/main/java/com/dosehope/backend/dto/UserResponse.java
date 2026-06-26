package com.dosehope.backend.dto;

import com.dosehope.backend.entity.Role;
import com.dosehope.backend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Safe user shape sent to the frontend. Mirrors the object stored in
 * frontend's getCurrentUser() — id, name, email, role, phone, address.
 * Deliberately excludes password.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private Role role;
    private String phone;
    private String address;

    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .phone(user.getPhone())
                .address(user.getAddress())
                .build();
    }
}
