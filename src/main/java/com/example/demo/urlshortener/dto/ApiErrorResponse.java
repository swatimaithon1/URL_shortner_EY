package com.example.demo.urlshortener.dto;

import java.time.Instant;

// Standard error payload returned by the global exception handler.
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {
}

