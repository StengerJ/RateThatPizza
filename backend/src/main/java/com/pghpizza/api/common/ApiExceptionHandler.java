package com.pghpizza.api.common;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<Map<String, String>> handleNotFound(NotFoundException exception, HttpServletRequest request) {
        log.warn("API not found error at {} {}: {}", request.getMethod(), request.getRequestURI(),
                exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(ConflictException.class)
    ResponseEntity<Map<String, String>> handleConflict(ConflictException exception, HttpServletRequest request) {
        log.warn("API conflict at {} {}: {}", request.getMethod(), request.getRequestURI(), exception.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler({ ForbiddenActionException.class, AuthorizationDeniedException.class })
    ResponseEntity<Map<String, String>> handleForbidden(RuntimeException exception, HttpServletRequest request) {
        log.warn("API forbidden response at {} {}: {}", request.getMethod(), request.getRequestURI(),
                exception.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Forbidden"));
    }

    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException exception,
            HttpServletRequest request) {
        log.warn("API authentication failure at {} {}", request.getMethod(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid credentials"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse("Invalid request");
        log.warn("API validation error at {} {}: {}", request.getMethod(), request.getRequestURI(), message);
        return ResponseEntity.badRequest().body(Map.of("message", message));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, String>> handleUnhandled(Exception exception, HttpServletRequest request) {
        log.error("Unhandled API error at {} {}", request.getMethod(), request.getRequestURI(), exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Unexpected server error"));
    }
}
