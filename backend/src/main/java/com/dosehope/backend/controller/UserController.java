package com.dosehope.backend.controller;

import com.dosehope.backend.dto.UserResponse;
import com.dosehope.backend.security.UserPrincipal;
import com.dosehope.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserResponse getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        return userService.getById(principal.getId());
    }

    @GetMapping("/volunteers")
    public List<UserResponse> getAllVolunteers() {
        return userService.getAllVolunteers();
    }

    @GetMapping("/ngos")
    public List<UserResponse> getAllNgos() {
        return userService.getAllNgos();
    }
}
