package com.backendlab.authservice.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super("Usuário não encontrado");
    }
}
