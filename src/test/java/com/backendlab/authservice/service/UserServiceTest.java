package com.backendlab.authservice.service;

import com.backendlab.authservice.dto.RegisterRequest;
import com.backendlab.authservice.entity.Role;
import com.backendlab.authservice.entity.User;
import com.backendlab.authservice.exception.UserAlreadyExistsException;
import com.backendlab.authservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String NAME = "Rodrigo";
    private static final String EMAIL = "rodrigo@example.com";
    private static final String RAW_PASSWORD = "secret123";
    private static final String ENCODED_PASSWORD = "$2a$10$encodedHash";

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void registerDeveSalvarUsuarioComSenhaCriptografadaERoleUser() {
        RegisterRequest request = new RegisterRequest(NAME, EMAIL, RAW_PASSWORD);

        when(repository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = userService.register(request);

        assertThat(saved.getName()).isEqualTo(NAME);
        assertThat(saved.getEmail()).isEqualTo(EMAIL);
        assertThat(saved.getPassword()).isEqualTo(ENCODED_PASSWORD);
        assertThat(saved.getRole()).isEqualTo(Role.USER);
        assertThat(saved.getCreatedAt()).isNotNull();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getPassword())
                .as("a senha em texto puro nunca deve ser persistida")
                .isEqualTo(ENCODED_PASSWORD)
                .isNotEqualTo(RAW_PASSWORD);
        verify(passwordEncoder).encode(RAW_PASSWORD);
    }

    @Test
    void registerDeveLancarExcecaoQuandoEmailJaExistir() {
        RegisterRequest request = new RegisterRequest(NAME, EMAIL, RAW_PASSWORD);
        when(repository.existsByEmail(EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Email já cadastrado");

        verify(repository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder);
    }
}
