package com.example.demo.urlshortener.repository;

import com.example.demo.urlshortener.model.ShortUrl;

import java.time.Instant;
import java.util.Optional;

public interface ShortUrlRepository {

    // Loads a shortcode with analytics details when available.
    Optional<ShortUrl> findByCode(String code);

    // Fast existence check used for alias collision detection.
    boolean existsByCode(String code);

    ShortUrl save(ShortUrl shortUrl);

    // Atomically increments click metrics for redirects.
    void recordClick(String code, Instant timestamp);

    void deleteByCode(String code);

    // Removes all expired links up to the supplied timestamp.
    int deleteExpired(Instant now);
}

