package com.example.demo.urlshortener.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;

// Input payload for short-link creation with bean-validation constraints.
public record CreateShortUrlRequest(
        // Target URL users will be redirected to.
        @NotBlank(message = "url is required")
        @Size(max = 2048, message = "url must be <= 2048 characters")
        String url,
        // Optional caller-provided alias; server generates one when omitted.
        @Pattern(regexp = "^[a-zA-Z0-9_-]{4,20}$", message = "customAlias must be 4-20 chars: letters, numbers, _ or -")
        String customAlias,
        // Optional expiry; if present it must be in the future.
        @Future(message = "expiresAt must be in the future")
        Instant expiresAt
) {
}

