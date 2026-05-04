package com.backendlab.authservice.exception;
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Email ou senha inválidos");
    }
}
