package com.backendlab.authservice.controller;

import com.backendlab.authservice.dto.AuthResponse;
import com.backendlab.authservice.dto.LoginRequest;
import com.backendlab.authservice.dto.RegisterRequest;
import com.backendlab.authservice.dto.UserResponse;
import com.backendlab.authservice.service.AuthService;
import com.backendlab.authservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Rotas públicas de cadastro e login")
@SecurityRequirements
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    @Operation(summary = "Cadastra um novo usuário com perfil USER")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário cadastrado"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "E-mail já cadastrado")
    })
    @PostMapping("/register")
    public UserResponse register(@RequestBody @Valid RegisterRequest request) {
        return UserResponse.from(userService.register(request));
    }

    @Operation(summary = "Autentica o usuário e retorna um token JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login realizado e token retornado"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "E-mail ou senha inválidos")
    })
    @PostMapping("/login")
    public AuthResponse login(@RequestBody @Valid LoginRequest request) {
        return authService.login(request);
    }
}
