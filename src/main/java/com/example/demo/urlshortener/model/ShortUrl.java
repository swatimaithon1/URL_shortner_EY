package com.example.demo.urlshortener.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class ShortUrl {

    private final String code;
    private final String originalUrl;
    private final Instant createdAt;
    private final Instant expiresAt;
    private final long clickCount;
    private final Instant lastAccessedAt;
    private final Map<LocalDate, Long> dailyClicks;

    public ShortUrl(String code, String originalUrl, Instant createdAt, Instant expiresAt) {
        // Convenience constructor for brand-new links with zero analytics.
        this(code, originalUrl, createdAt, expiresAt, 0L, null, Map.of());
    }

    public ShortUrl(
            String code,
            String originalUrl,
            Instant createdAt,
            Instant expiresAt,
            long clickCount,
            Instant lastAccessedAt,
            Map<LocalDate, Long> dailyClicks
    ) {
        // Enforces required core fields when reconstructing from persistence.
        this.code = Objects.requireNonNull(code, "code is required");
        this.originalUrl = Objects.requireNonNull(originalUrl, "originalUrl is required");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.expiresAt = expiresAt;
        this.clickCount = clickCount;
        this.lastAccessedAt = lastAccessedAt;
        // Defensive copy keeps analytics snapshot immutable from outside callers.
        this.dailyClicks = Collections.unmodifiableMap(new LinkedHashMap<>(dailyClicks));
    }

    public String getCode() {
        return code;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public long getClickCount() {
        return clickCount;
    }

    public Instant getLastAccessedAt() {
        return lastAccessedAt;
    }

    public boolean isExpired(Instant now) {
        // Expiry is inclusive: equal timestamps count as expired.
        return expiresAt != null && !expiresAt.isAfter(now);
    }

    public Map<LocalDate, Long> snapshotDailyClicks() {
        // Returns a mutable copy so callers cannot mutate internal state.
        return new LinkedHashMap<>(dailyClicks);
    }
}

