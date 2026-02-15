package com.example.packageaggregator.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(PackageNotFoundException.class)
    public ResponseEntity<ErrorBody> handlePackageNotFound(PackageNotFoundException ex) {
        log.warn("Package not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorBody.builder()
                .status(404)
                .error("Not Found")
                .message(ex.getMessage())
                .timestamp(Instant.now().toString())
                .build());
    }

    @ExceptionHandler(ExternalServiceUnavailableException.class)
    public ResponseEntity<ErrorBody> handleExternalServiceUnavailable(ExternalServiceUnavailableException ex) {
        log.error("External service unavailable: {}", ex.getMessage());
        String message = ex.getMessage() != null && !ex.getMessage().isBlank()
                ? ex.getMessage()
                : "A required external service is temporarily unavailable.";
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ErrorBody.builder()
                .status(503)
                .error("Service Unavailable")
                .message(message)
                .timestamp(Instant.now().toString())
                .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorBody> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("Validation error: {}", message);
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorBody.builder()
                .status(400)
                .error("Bad Request")
                .message(message)
                .timestamp(Instant.now().toString())
                .validationErrors(errors)
                .build());
    }

    @ExceptionHandler(InvalidProductException.class)
    public ResponseEntity<ErrorBody> handleInvalidProduct(InvalidProductException ex) {
        log.warn("Invalid product: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorBody.builder()
                .status(400)
                .error("Bad Request")
                .message(ex.getMessage())
                .timestamp(Instant.now().toString())
                .build());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorBody> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorBody.builder()
                .status(400)
                .error("Bad Request")
                .message(ex.getMessage())
                .timestamp(Instant.now().toString())
                .build());
    }

    @lombok.Getter
    @lombok.Builder
    private static class ErrorBody {
        private final int status;
        private final String error;
        private final String message;
        private final String timestamp;
        @lombok.Builder.Default
        private final Map<String, String> validationErrors = null;
    }
}
