package com.example.demo.urlshortener.api;

import com.example.demo.urlshortener.dto.CreateShortUrlRequest;
import com.example.demo.urlshortener.dto.ShortUrlResponse;
import com.example.demo.urlshortener.dto.UrlAnalyticsResponse;
import com.example.demo.urlshortener.model.ShortUrl;
import com.example.demo.urlshortener.service.ShortUrlService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/urls")
public class UrlShortenerController {

    private final ShortUrlService shortUrlService;
    private final String baseUrl;

    public UrlShortenerController(ShortUrlService shortUrlService, @Value("${url-shortener.base-url:http://localhost:8080}") String baseUrl) {
        this.shortUrlService = shortUrlService;
        // Normalizes configured base URL to avoid accidental double slashes.
        this.baseUrl = trimTrailingSlash(baseUrl);
    }

    @PostMapping
    public ResponseEntity<ShortUrlResponse> create( @RequestBody CreateShortUrlRequest request) {
        // Delegates URL validation, collision checks, and persistence to the service layer.
        ShortUrl shortUrl = shortUrlService.createShortUrl(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(shortUrl));
    }

    @GetMapping("/{code:[a-zA-Z0-9_-]{4,20}}")
    public ShortUrlResponse getByCode(@PathVariable String code) {
        // Returns stored metadata without recording a click.
        return toResponse(shortUrlService.getByCode(code));
    }

    @GetMapping("/{code:[a-zA-Z0-9_-]{4,20}}/analytics")
    public UrlAnalyticsResponse analytics(@PathVariable String code) {
        // Uses the same fetch path so expiry and not-found rules stay consistent.
        ShortUrl shortUrl = shortUrlService.getByCode(code);
        return new UrlAnalyticsResponse(
                shortUrl.getCode(),
                shortUrl.getOriginalUrl(),
                shortUrl.getClickCount(),
                shortUrl.getLastAccessedAt(),
                shortUrl.snapshotDailyClicks()
        );
    }

    @DeleteMapping("/{code:[a-zA-Z0-9_-]{4,20}}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String code) {
        // Delete is idempotent from client perspective but validates existence first.
        shortUrlService.deleteByCode(code);
    }

    private ShortUrlResponse toResponse(ShortUrl shortUrl) {
        // Builds absolute short URL so clients can use the link directly.
        return new ShortUrlResponse(
                shortUrl.getCode(),
                baseUrl + "/" + shortUrl.getCode(),
                shortUrl.getOriginalUrl(),
                shortUrl.getCreatedAt(),
                shortUrl.getExpiresAt(),
                shortUrl.getClickCount(),
                shortUrl.getLastAccessedAt()
        );
    }

    private String trimTrailingSlash(String candidate) {
        // Keeps base URL canonical across configuration variants.
        if (candidate.endsWith("/")) {
            return candidate.substring(0, candidate.length() - 1);
        }
        return candidate;
    }
}

