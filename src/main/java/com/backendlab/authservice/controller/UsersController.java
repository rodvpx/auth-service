package com.backendlab.authservice.controller;

import com.backendlab.authservice.dto.AuthResponse;
import com.backendlab.authservice.dto.LoginRequest;
import com.backendlab.authservice.dto.RegisterRequest;
import com.backendlab.authservice.entity.User;
import com.backendlab.authservice.service.AuthService;
import com.backendlab.authservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UsersController {

    private final UserService service;
    private final AuthService authService;

    @PostMapping("/register")
    public User register(@RequestBody @Valid RegisterRequest request) {

        return service.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody @Valid LoginRequest request) {

        return authService.login(request);
    }
}
