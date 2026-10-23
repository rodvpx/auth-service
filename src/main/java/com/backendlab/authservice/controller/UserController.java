package com.backendlab.authservice.controller;

import com.backendlab.authservice.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Rotas que exigem um JWT válido")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class UserController {

    @Operation(summary = "Retorna o e-mail do usuário autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário autenticado"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado")
    })
    @GetMapping("/me")
    public String me(Authentication authentication) {
        return "Usuário autenticado: " + authentication.getName();
    }
}
