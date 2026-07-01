package com.poly.BE_main.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<?> validation(MethodArgumentNotValidException ex) {
        Map<String, String> fields = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> fields.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(Map.of("code", "VALIDATION_ERROR", "message", "Invalid request",
                "fieldErrors", fields, "timestamp", Instant.now().toString()));
    }

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<?> status(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("code", "REQUEST_REJECTED",
                "message", ex.getReason() == null ? "Request rejected" : ex.getReason(),
                "timestamp", Instant.now().toString()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<?> badRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("code", "BAD_REQUEST", "message", ex.getMessage(),
                "timestamp", Instant.now().toString()));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<?> unexpected(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("code", "INTERNAL_ERROR",
                "message", "An unexpected error occurred", "timestamp", Instant.now().toString()));
    }
}
