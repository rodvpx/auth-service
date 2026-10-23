package com.backendlab.authservice.controller;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserControllerTest {

    private final UserController controller = new UserController();

    @Test
    void givenAuthenticatedUser_whenMeIsCalled_thenReturnsEmail() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "user@example.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        String response = controller.me(authentication);

        assertThat(response).isEqualTo("Usuário autenticado: user@example.com");
    }
}
