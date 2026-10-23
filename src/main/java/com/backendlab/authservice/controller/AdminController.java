package com.backendlab.authservice.controller;

import com.backendlab.authservice.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Administração", description = "Rotas exclusivas para o perfil ADMIN")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class AdminController {

    @Operation(summary = "Rota de teste da área administrativa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Acesso autorizado para ADMIN"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Usuário autenticado sem perfil ADMIN")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/test")
    public String adminTest() {
        return "Área ADMIN 🔥";
    }
}
