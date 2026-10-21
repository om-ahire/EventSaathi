package com.eventsaathi.controller;

import com.eventsaathi.dto.ApiDtos.AuthResponse;
import com.eventsaathi.dto.ApiDtos.LoginRequest;
import com.eventsaathi.dto.ApiDtos.RegisterRequest;
import com.eventsaathi.dto.ApiDtos.UserResponse;
import com.eventsaathi.security.UserPrincipal;
import com.eventsaathi.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal UserPrincipal principal) {
        return authService.currentUser(principal);
    }
}
