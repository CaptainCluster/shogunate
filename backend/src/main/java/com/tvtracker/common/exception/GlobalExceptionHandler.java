package com.tvtracker.common.exception;

import java.time.Instant;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApiException(ApiException ex) {
        return errorResponse(ex.getMessage(), ex.getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse("Validation failed");
        return errorResponse(message, 400);
    }

    @ExceptionHandler({AuthenticationException.class, AccessDeniedException.class})
    public ResponseEntity<Map<String, Object>> handleSecurity(Exception ex) {
        int status = ex instanceof AccessDeniedException ? 403 : 401;
        return errorResponse(ex.getMessage(), status);
    }

    private ResponseEntity<Map<String, Object>> errorResponse(String message, int status) {
        return ResponseEntity.status(status)
                .body(Map.of(
                        "message", message,
                        "status", status,
                        "timestamp", Instant.now().toString()));
    }
}
