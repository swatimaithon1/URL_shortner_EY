package com.example.demo.urlshortener.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

// Analytics payload with total and day-wise click counts.
public record UrlAnalyticsResponse(
        String code,
        String originalUrl,
        long totalClicks,
        Instant lastAccessedAt,
        Map<LocalDate, Long> dailyClicks
) {
}

