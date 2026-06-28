package com.example.demo.urlshortener.service;

import com.example.demo.urlshortener.dto.CreateShortUrlRequest;
import com.example.demo.urlshortener.exception.ResourceConflictException;
import com.example.demo.urlshortener.exception.ResourceExpiredException;
import com.example.demo.urlshortener.exception.ResourceNotFoundException;
import com.example.demo.urlshortener.model.ShortUrl;
import com.example.demo.urlshortener.repository.ShortUrlRepository;
import com.example.demo.urlshortener.validation.UrlSafetyValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;

@Service
public class DefaultShortUrlService implements ShortUrlService {

    private final ShortUrlRepository repository;
    private final ShortCodeGenerator shortCodeGenerator;
    private final UrlSafetyValidator urlSafetyValidator;
    private final Clock clock;
    private final int codeLength;
    private final int maxGenerationAttempts;

    public DefaultShortUrlService(
            ShortUrlRepository repository,
            ShortCodeGenerator shortCodeGenerator,
            UrlSafetyValidator urlSafetyValidator,
            @Value("${url-shortener.code-length:7}") int codeLength,
            @Value("${url-shortener.max-generation-attempts:8}") int maxGenerationAttempts
    ) {
        this.repository = repository;
        this.shortCodeGenerator = shortCodeGenerator;
        this.urlSafetyValidator = urlSafetyValidator;
        this.clock = Clock.systemUTC();
        this.codeLength = codeLength;
        this.maxGenerationAttempts = maxGenerationAttempts;
    }

    @Override
    public ShortUrl createShortUrl(CreateShortUrlRequest request) {
        // Rejects malformed or unsafe targets before any persistence work.
        urlSafetyValidator.validateAndParse(request.url());

        Instant now = Instant.now(clock);
        // Honors caller alias when present; otherwise generates one.
        String code = request.customAlias() == null ? generateUniqueCode() : request.customAlias();

        if (repository.existsByCode(code)) {
            throw new ResourceConflictException("short code already exists: " + code);
        }

        ShortUrl shortUrl = new ShortUrl(code, request.url(), now, request.expiresAt());
        return repository.save(shortUrl);
    }

    @Override
    public ShortUrl resolveAndTrack(String code) {
        // Reuses read path so not-found and expiry rules stay centralized.
        ShortUrl shortUrl = getByCode(code);
        // Writes analytics after successful resolution.
        repository.recordClick(code, Instant.now(clock));
        return shortUrl;
    }

    @Override
    public ShortUrl getByCode(String code) {
        ShortUrl shortUrl = repository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("short code not found: " + code));

        if (shortUrl.isExpired(Instant.now(clock))) {
            // Eagerly removes expired entry so future reads fail fast.
            repository.deleteByCode(code);
            throw new ResourceExpiredException("short code is expired: " + code);
        }

        return shortUrl;
    }

    @Override
    public void deleteByCode(String code) {
        if (!repository.existsByCode(code)) {
            throw new ResourceNotFoundException("short code not found: " + code);
        }
        repository.deleteByCode(code);
    }

    @Override
    public int cleanupExpired() {
        return repository.deleteExpired(Instant.now(clock));
    }

    @Scheduled(fixedDelayString = "${url-shortener.cleanup-delay-ms:60000}")
    void scheduledCleanup() {
        // Background maintenance to keep storage free of expired links.
        cleanupExpired();
    }

    private String generateUniqueCode() {
        // Bounded retry loop prevents endless collisions in pathological cases.
        for (int attempts = 0; attempts < maxGenerationAttempts; attempts++) {
            String code = shortCodeGenerator.generate(codeLength);
            if (!repository.existsByCode(code)) {
                return code;
            }
        }
        throw new ResourceConflictException("unable to generate a unique short code");
    }
}

