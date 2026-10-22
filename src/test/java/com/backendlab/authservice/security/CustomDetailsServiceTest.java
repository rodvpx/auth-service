package com.backendlab.authservice.security;

import com.backendlab.authservice.entity.Role;
import com.backendlab.authservice.entity.User;
import com.backendlab.authservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomDetailsServiceTest {

    private static final String EMAIL = "admin@example.com";
    private static final String ENCODED_PASSWORD = "$2a$10$encodedHash";

    @Mock
    private UserRepository repository;

    @InjectMocks
    private CustomDetailsService customDetailsService;

    @Test
    void loadUserByUsernameDeveRetornarUserDetailsDoUsuario() {
        User user = User.builder()
                .email(EMAIL)
                .password(ENCODED_PASSWORD)
                .role(Role.ADMIN)
                .build();
        when(repository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        UserDetails userDetails = customDetailsService.loadUserByUsername(EMAIL);

        assertThat(userDetails.getUsername()).isEqualTo(EMAIL);
        assertThat(userDetails.getPassword()).isEqualTo(ENCODED_PASSWORD);
        assertThat(userDetails.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");
        verify(repository).findByEmail(EMAIL);
    }

    @Test
    void loadUserByUsernameDeveLancarExcecaoQuandoUsuarioNaoExistir() {
        when(repository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customDetailsService.loadUserByUsername(EMAIL))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Usuário não encontrado");
    }
}
