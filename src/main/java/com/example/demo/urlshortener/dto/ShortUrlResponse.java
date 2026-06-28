package com.example.demo.urlshortener.dto;

import java.time.Instant;

// Public response model returned for create and lookup endpoints.
public record ShortUrlResponse(
        String code,
        String shortUrl,
        String originalUrl,
        Instant createdAt,
        Instant expiresAt,
        long clickCount,
        Instant lastAccessedAt
) {
}

