package com.example.demo.urlshortener.service;

import com.example.demo.urlshortener.dto.CreateShortUrlRequest;
import com.example.demo.urlshortener.model.ShortUrl;

public interface ShortUrlService {

    // Creates a new short link from client input.
    ShortUrl createShortUrl(CreateShortUrlRequest request);

    // Resolves a link for redirect and tracks a click.
    ShortUrl resolveAndTrack(String code);

    // Fetches link metadata without incrementing clicks.
    ShortUrl getByCode(String code);

    void deleteByCode(String code);

    // Triggers expired-link cleanup and returns deleted row count.
    int cleanupExpired();
}

