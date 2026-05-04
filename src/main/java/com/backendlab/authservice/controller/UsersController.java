package com.backendlab.authservice.controller;

import com.backendlab.authservice.dto.RegisterRequest;
import com.backendlab.authservice.entity.User;
import com.backendlab.authservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UsersController {

    private final UserService service;

    @PostMapping("/register")
    public User register(@RequestBody @Valid RegisterRequest request) {

        return service.register(request);
    }
}
