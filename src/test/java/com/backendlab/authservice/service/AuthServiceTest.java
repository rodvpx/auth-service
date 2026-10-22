package com.backendlab.authservice.service;

import com.backendlab.authservice.dto.AuthResponse;
import com.backendlab.authservice.dto.LoginRequest;
import com.backendlab.authservice.entity.Role;
import com.backendlab.authservice.entity.User;
import com.backendlab.authservice.exception.InvalidCredentialsException;
import com.backendlab.authservice.repository.UserRepository;
import com.backendlab.authservice.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String EMAIL = "user@example.com";
    private static final String RAW_PASSWORD = "secret123";
    private static final String ENCODED_PASSWORD = "$2a$10$encodedHash";
    private static final String TOKEN = "jwt-token";

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User user() {
        return User.builder()
                .name("Usuário Teste")
                .email(EMAIL)
                .password(ENCODED_PASSWORD)
                .role(Role.USER)
                .build();
    }

    @Test
    void loginDeveRetornarTokenQuandoCredenciaisForemValidas() {
        User user = user();
        when(repository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn(TOKEN);

        AuthResponse response = authService.login(new LoginRequest(EMAIL, RAW_PASSWORD));

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo(TOKEN);
        verify(repository).findByEmail(EMAIL);
        verify(passwordEncoder).matches(RAW_PASSWORD, ENCODED_PASSWORD);
        verify(jwtService).generateToken(user);
    }

    @Test
    void loginDeveLancarExcecaoQuandoUsuarioNaoExistir() {
        when(repository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest(EMAIL, RAW_PASSWORD)))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Email ou senha inválidos");

        verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    void loginDeveLancarExcecaoQuandoSenhaNaoConferir() {
        when(repository.findByEmail(EMAIL)).thenReturn(Optional.of(user()));
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest(EMAIL, RAW_PASSWORD)))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Email ou senha inválidos");

        verify(jwtService, never()).generateToken(any());
    }
}
