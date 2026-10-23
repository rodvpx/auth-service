package com.backendlab.authservice.exception;

public record FieldErrorResponse(
        String field,
        String message
) {
}
