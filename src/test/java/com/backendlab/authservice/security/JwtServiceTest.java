package com.backendlab.authservice.security;

import com.backendlab.authservice.entity.Role;
import com.backendlab.authservice.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "my-super-secret-key-very-long-and-safe-123456789";
    private static final long EXPIRATION = 3_600_000L;
    private static final String EMAIL = "user@example.com";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "expiration", EXPIRATION);
    }

    private User user(Role role) {
        return User.builder()
                .name("Usuário Teste")
                .email(EMAIL)
                .password("$2a$10$encodedHash")
                .role(role)
                .build();
    }

    private Claims parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(SECRET.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    @Test
    void generateTokenDeveIncluirEmailRoleEExpiracao() {
        String token = jwtService.generateToken(user(Role.ADMIN));

        assertThat(token).isNotBlank();

        Claims claims = parse(token);
        assertThat(claims.getSubject()).isEqualTo(EMAIL);
        assertThat(claims.get("role", String.class)).isEqualTo("ADMIN");
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isAfter(new Date());
    }

    @Test
    void extractEmailDeveRetornarSubjectDoToken() {
        String token = jwtService.generateToken(user(Role.USER));

        assertThat(jwtService.extractEmail(token)).isEqualTo(EMAIL);
    }

    @Test
    void isTokenValidDeveRetornarTrueParaTokenValido() {
        String token = jwtService.generateToken(user(Role.USER));

        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValidDeveRetornarFalseParaTokenExpirado() {
        ReflectionTestUtils.setField(jwtService, "expiration", -60_000L);
        String expiredToken = jwtService.generateToken(user(Role.USER));

        assertThat(jwtService.isTokenValid(expiredToken)).isFalse();
    }

    @Test
    void isTokenValidDeveRetornarFalseParaTokenInvalido() {
        assertThat(jwtService.isTokenValid("token-invalido")).isFalse();
    }
}
