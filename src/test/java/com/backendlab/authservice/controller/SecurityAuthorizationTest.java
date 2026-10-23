package com.backendlab.authservice.controller;

import com.backendlab.authservice.entity.Role;
import com.backendlab.authservice.entity.User;
import com.backendlab.authservice.repository.UserRepository;
import com.backendlab.authservice.security.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityAuthorizationTest {

    private static final String SECRET = "test-super-secret-key-very-long-and-safe-123456789";
    private static final String USER_EMAIL = "user@example.com";
    private static final String ADMIN_EMAIL = "admin@example.com";
    private static final String PASSWORD = "password123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String userToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        User user = repository.save(user(USER_EMAIL, Role.USER));
        User admin = repository.save(user(ADMIN_EMAIL, Role.ADMIN));

        userToken = jwtService.generateToken(user);
        adminToken = jwtService.generateToken(admin);
    }

    @Test
    void givenPublicLoginRoute_whenValidCredentialsWithoutToken_thenReturns200() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"user@example.com","password":"password123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void givenPublicRegisterRoute_whenValidRequestWithoutToken_thenReturns200() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Novo Usuário","email":"novo@example.com","password":"password123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("novo@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void givenUsersRoute_whenNoToken_thenReturns401WithJsonBody() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Autenticação necessária"));
    }

    @Test
    void givenUsersRoute_whenMalformedToken_thenReturns401() throws Exception {
        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer token-malformado"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void givenUsersRoute_whenExpiredToken_thenReturns401() throws Exception {
        String expiredToken = Jwts.builder()
                .setSubject(USER_EMAIL)
                .setIssuedAt(new Date(System.currentTimeMillis() - 120_000))
                .setExpiration(new Date(System.currentTimeMillis() - 60_000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes()), SignatureAlgorithm.HS256)
                .compact();

        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void givenUsersRoute_whenValidUserToken_thenReturnsAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(content().string("Usuário autenticado: " + USER_EMAIL));
    }

    @Test
    void givenAdminRoute_whenNoToken_thenReturns401() throws Exception {
        mockMvc.perform(get("/admin/test"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void givenAdminRoute_whenUserToken_thenReturns403WithJsonBody() throws Exception {
        mockMvc.perform(get("/admin/test")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("Acesso negado"));
    }

    @Test
    void givenAdminRoute_whenAdminToken_thenReturns200() throws Exception {
        mockMvc.perform(get("/admin/test")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().string("Área ADMIN 🔥"));
    }

    @Test
    void givenOpenApiDocs_whenNoToken_thenReturns200() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Auth Service API"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"));
    }

    private User user(String email, Role role) {
        return User.builder()
                .name(role == Role.ADMIN ? "Admin" : "Usuário")
                .email(email)
                .password(passwordEncoder.encode(PASSWORD))
                .role(role)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
