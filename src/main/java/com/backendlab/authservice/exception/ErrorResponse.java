package com.backendlab.authservice.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        int status,
        String error,
        String message,
        LocalDateTime timestamp,
        List<FieldErrorResponse> fields
) {

    public ErrorResponse(int status, String error, String message, LocalDateTime timestamp) {
        this(status, error, message, timestamp, null);
    }
}
