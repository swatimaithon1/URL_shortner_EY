package com.example.demo.urlshortener.api;

import com.example.demo.urlshortener.model.ShortUrl;
import com.example.demo.urlshortener.service.ShortUrlService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class RedirectController {

    private final ShortUrlService shortUrlService;

    public RedirectController(ShortUrlService shortUrlService) {
        this.shortUrlService = shortUrlService;
    }

    @GetMapping("/{code:[a-zA-Z0-9_-]{4,20}}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        // Resolves code and increments analytics in one service call.
        ShortUrl shortUrl = shortUrlService.resolveAndTrack(code);
        // Returns a standard 302 redirect to the original destination.
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(shortUrl.getOriginalUrl())).build();
    }
}

