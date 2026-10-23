package com.backendlab.authservice.config;

import com.backendlab.authservice.entity.Role;
import com.backendlab.authservice.entity.User;
import com.backendlab.authservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserInitializerTest {

    private static final String EMAIL = "admin@example.com";
    private static final String PASSWORD = "senha-admin-segura";
    private static final String ENCODED = "$2a$10$encodedHash";

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void givenConfiguredAdmin_whenItDoesNotExist_thenCreatesAdminWithEncodedPassword() throws Exception {
        AdminUserInitializer initializer =
                new AdminUserInitializer(repository, passwordEncoder, EMAIL, PASSWORD, true);
        when(repository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED);

        initializer.run(null);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(repository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo(EMAIL);
        assertThat(saved.getPassword()).isEqualTo(ENCODED).isNotEqualTo(PASSWORD);
        assertThat(saved.getRole()).isEqualTo(Role.ADMIN);
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void givenConfiguredAdmin_whenItAlreadyExists_thenDoesNotCreateAnother() throws Exception {
        AdminUserInitializer initializer =
                new AdminUserInitializer(repository, passwordEncoder, EMAIL, PASSWORD, true);
        when(repository.existsByEmail(EMAIL)).thenReturn(true);

        initializer.run(null);

        verify(repository, never()).save(any(User.class));
    }

    @Test
    void givenNoAdminConfiguration_whenNotRequired_thenDoesNothing() throws Exception {
        AdminUserInitializer initializer =
                new AdminUserInitializer(repository, passwordEncoder, "", "", false);

        initializer.run(null);

        verify(repository, never()).existsByEmail(any());
        verify(repository, never()).save(any(User.class));
    }

    @Test
    void givenNoAdminConfiguration_whenRequired_thenFailsClearly() {
        AdminUserInitializer initializer =
                new AdminUserInitializer(repository, passwordEncoder, "", "", true);

        assertThatThrownBy(() -> initializer.run(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ADMIN_EMAIL e ADMIN_PASSWORD são obrigatórios");
    }

    @Test
    void givenOnlyEmailConfigured_thenFailsClearly() {
        AdminUserInitializer initializer =
                new AdminUserInitializer(repository, passwordEncoder, EMAIL, "", true);

        assertThatThrownBy(() -> initializer.run(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("devem ser definidos juntos");
    }

    @Test
    void givenOnlyPasswordConfigured_thenFailsClearly() {
        AdminUserInitializer initializer =
                new AdminUserInitializer(repository, passwordEncoder, "", PASSWORD, true);

        assertThatThrownBy(() -> initializer.run(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("devem ser definidos juntos");
    }
}
