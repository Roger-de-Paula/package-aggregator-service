package com.example.packageaggregator.exception;

/**
 * Thrown when a product id is invalid (not found or missing required data such as price).
 * Mapped to 400 Bad Request by {@link com.example.packageaggregator.exception.GlobalExceptionHandler}.
 */
public class InvalidProductException extends RuntimeException {

    public InvalidProductException(String message) {
        super(message);
    }

    public InvalidProductException(String message, Throwable cause) {
        super(message, cause);
    }
}
