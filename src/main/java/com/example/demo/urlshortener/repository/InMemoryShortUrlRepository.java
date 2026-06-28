package com.example.demo.urlshortener.repository;

import com.example.demo.urlshortener.model.ShortUrl;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class InMemoryShortUrlRepository implements ShortUrlRepository {

    private final ConcurrentMap<String, ShortUrl> storage = new ConcurrentHashMap<>();

    @Override
    public Optional<ShortUrl> findByCode(String code) {
        return Optional.ofNullable(storage.get(code));
    }

    @Override
    public boolean existsByCode(String code) {
        return storage.containsKey(code);
    }

    @Override
    public ShortUrl save(ShortUrl shortUrl) {
        storage.put(shortUrl.getCode(), shortUrl);
        return shortUrl;
    }

    @Override
    public void recordClick(String code, Instant timestamp) {
        storage.computeIfPresent(code, (ignored, existing) -> {
            Map<LocalDate, Long> dailyClicks = new LinkedHashMap<>(existing.snapshotDailyClicks());
            LocalDate clickDate = LocalDate.ofInstant(timestamp, ZoneOffset.UTC);
            dailyClicks.merge(clickDate, 1L, Long::sum);
            return new ShortUrl(
                    existing.getCode(),
                    existing.getOriginalUrl(),
                    existing.getCreatedAt(),
                    existing.getExpiresAt(),
                    existing.getClickCount() + 1,
                    timestamp,
                    dailyClicks
            );
        });
    }

    @Override
    public void deleteByCode(String code) {
        storage.remove(code);
    }

    @Override
    public int deleteExpired(Instant now) {
        int before = storage.size();
        storage.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
        return before - storage.size();
    }
}

