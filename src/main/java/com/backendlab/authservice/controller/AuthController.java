package com.backendlab.authservice.controller;

import com.backendlab.authservice.dto.AuthResponse;
import com.backendlab.authservice.dto.LoginRequest;
import com.backendlab.authservice.dto.RegisterRequest;
import com.backendlab.authservice.entity.User;
import com.backendlab.authservice.service.AuthService;
import com.backendlab.authservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/register")
    public User register(@RequestBody @Valid RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody @Valid LoginRequest request) {
        return authService.login(request);
    }
}
