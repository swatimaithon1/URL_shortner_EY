package com.example.demo.urlshortener.api;

import com.example.demo.urlshortener.dto.ApiErrorResponse;
import com.example.demo.urlshortener.exception.InvalidUrlException;
import com.example.demo.urlshortener.exception.ResourceConflictException;
import com.example.demo.urlshortener.exception.ResourceExpiredException;
import com.example.demo.urlshortener.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException exception, HttpServletRequest request) {
        // Missing shortcode maps to 404 for REST clients.
        return errorResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(ResourceExpiredException.class)
    public ResponseEntity<ApiErrorResponse> handleExpired(ResourceExpiredException exception, HttpServletRequest request) {
        // Expired resources return 410 to indicate permanent unavailability.
        return errorResponse(HttpStatus.GONE, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler({ResourceConflictException.class, InvalidUrlException.class})
    public ResponseEntity<ApiErrorResponse> handleConflict(RuntimeException exception, HttpServletRequest request) {
        // Conflict for duplicate alias; bad request for invalid URL input.
        HttpStatus status = exception instanceof ResourceConflictException ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
        return errorResponse(status, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        // Exposes the first field error to keep validation feedback concise.
        FieldError fieldError = exception.getBindingResult().getFieldError();
        String message = fieldError == null ? "validation error" : fieldError.getField() + " " + fieldError.getDefaultMessage();
        return errorResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
    }

    private ResponseEntity<ApiErrorResponse> errorResponse(HttpStatus status, String message, String path) {
        // Centralized error envelope keeps response shape consistent.
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path
        ));
    }
}

