package com.backendlab.authservice.exception;

import com.backendlab.authservice.dto.RegisterRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.Set;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;
    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        mockMvc = MockMvcBuilders.standaloneSetup(new ThrowingController())
                .setControllerAdvice(handler)
                .build();
    }

    @Test
    void givenEmailAlreadyExists_whenHandled_thenReturns409WithErrorBody() throws Exception {
        mockMvc.perform(get("/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Email já cadastrado"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.fields").doesNotExist());
    }

    @Test
    void givenInvalidCredentials_whenHandled_thenReturns401WithErrorBody() throws Exception {
        mockMvc.perform(get("/unauthorized"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Email ou senha inválidos"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void givenUserNotFound_whenHandled_thenReturns404WithErrorBody() throws Exception {
        mockMvc.perform(get("/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Usuário não encontrado"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void givenInvalidRegisterRequest_whenHandled_thenReturns400WithFieldMessages() throws Exception {
        mockMvc.perform(post("/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","email":"email-invalido","password":"123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Erro de validação"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.fields[*].field",
                        containsInAnyOrder("name", "email", "password")))
                .andExpect(jsonPath("$.fields[*].message").isNotEmpty());
    }

    @Test
    void givenValidRegisterRequest_whenHandled_thenReturns200() throws Exception {
        mockMvc.perform(post("/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Maria","email":"maria@example.com","password":"senha-segura"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void givenConstraintViolation_whenHandled_thenReturns400WithFieldMessage() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(violation.getPropertyPath()).thenReturn(path);
        when(path.toString()).thenReturn("email");
        when(violation.getMessage()).thenReturn("não pode estar em branco");

        var response = handler.handleConstraintViolation(
                new ConstraintViolationException(Set.of(violation)));

        org.assertj.core.api.Assertions.assertThat(response.getStatusCode().value()).isEqualTo(400);
        org.assertj.core.api.Assertions.assertThat(response.getBody()).isNotNull();
        org.assertj.core.api.Assertions.assertThat(response.getBody().fields())
                .containsExactly(new FieldErrorResponse("email", "não pode estar em branco"));
    }

    @RestController
    static class ThrowingController {

        @GetMapping("/conflict")
        void conflict() {
            throw new UserAlreadyExistsException("Email já cadastrado");
        }

        @GetMapping("/unauthorized")
        void unauthorized() {
            throw new InvalidCredentialsException();
        }

        @GetMapping("/not-found")
        void notFound() {
            throw new UserNotFoundException();
        }

        @PostMapping("/validation")
        void validation(@RequestBody @Valid RegisterRequest request) {
        }
    }
}
