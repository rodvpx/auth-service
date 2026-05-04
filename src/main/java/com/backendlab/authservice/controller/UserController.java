package com.backendlab.authservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    @GetMapping("/me")
    public String me(Authentication authentication) {
        return "Usuário autenticado: " + authentication.getName();
    }
}
