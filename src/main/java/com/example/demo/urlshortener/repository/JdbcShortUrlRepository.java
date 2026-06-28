package com.example.demo.urlshortener.repository;

import com.example.demo.urlshortener.model.ShortUrl;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@Primary
public class JdbcShortUrlRepository implements ShortUrlRepository {

    private static final RowMapper<ShortUrl> SHORT_URL_ROW_MAPPER = new RowMapper<>() {
        @Override
        public ShortUrl mapRow(ResultSet rs, int rowNum) throws SQLException {
            // Base row excludes per-day analytics, which are loaded separately.
            return new ShortUrl(
                    rs.getString("code"),
                    rs.getString("original_url"),
                    rs.getTimestamp("created_at").toInstant(),
                    toInstant(rs.getTimestamp("expires_at")),
                    rs.getLong("click_count"),
                    toInstant(rs.getTimestamp("last_accessed_at")),
                    Map.of()
            );
        }
    };

    private final JdbcTemplate jdbcTemplate;

    public JdbcShortUrlRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<ShortUrl> findByCode(String code) {
        // Query main table first, then hydrate daily-click stats.
        List<ShortUrl> shortUrls = jdbcTemplate.query(
                "SELECT code, original_url, created_at, expires_at, click_count, last_accessed_at FROM short_urls WHERE code = ?",
                SHORT_URL_ROW_MAPPER,
                code
        );

        if (shortUrls.isEmpty()) {
            return Optional.empty();
        }

        ShortUrl base = shortUrls.getFirst();
        Map<LocalDate, Long> dailyClicks = loadDailyClicks(code);

        return Optional.of(new ShortUrl(
                base.getCode(),
                base.getOriginalUrl(),
                base.getCreatedAt(),
                base.getExpiresAt(),
                base.getClickCount(),
                base.getLastAccessedAt(),
                dailyClicks
        ));
    }

    @Override
    public boolean existsByCode(String code) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM short_urls WHERE code = ?", Integer.class, code);
        return count != null && count > 0;
    }

    @Override
    public ShortUrl save(ShortUrl shortUrl) {
        jdbcTemplate.update(
                "INSERT INTO short_urls (code, original_url, created_at, expires_at, click_count, last_accessed_at) VALUES (?, ?, ?, ?, ?, ?)",
                shortUrl.getCode(),
                shortUrl.getOriginalUrl(),
                Timestamp.from(shortUrl.getCreatedAt()),
                toTimestamp(shortUrl.getExpiresAt()),
                shortUrl.getClickCount(),
                toTimestamp(shortUrl.getLastAccessedAt())
        );
        return shortUrl;
    }

    @Override
    @Transactional
    public void recordClick(String code, Instant timestamp) {
        // Updates total click count and last access time in one statement.
        jdbcTemplate.update(
                "UPDATE short_urls SET click_count = click_count + 1, last_accessed_at = ? WHERE code = ?",
                Timestamp.from(timestamp),
                code
        );

        LocalDate clickDate = LocalDate.ofInstant(timestamp, ZoneOffset.UTC);
        // Upsert-like flow: update existing day bucket, else insert it.
        int rowsUpdated = jdbcTemplate.update(
                "UPDATE url_daily_clicks SET clicks = clicks + 1 WHERE code = ? AND click_date = ?",
                code,
                clickDate
        );

        if (rowsUpdated == 0) {
            jdbcTemplate.update(
                    "INSERT INTO url_daily_clicks (code, click_date, clicks) VALUES (?, ?, 1)",
                    code,
                    clickDate
            );
        }
    }

    @Override
    public void deleteByCode(String code) {
        jdbcTemplate.update("DELETE FROM short_urls WHERE code = ?", code);
    }

    @Override
    public int deleteExpired(Instant now) {
        // Cleanup task removes all links whose expiry is in the past.
        return jdbcTemplate.update("DELETE FROM short_urls WHERE expires_at IS NOT NULL AND expires_at <= ?", Timestamp.from(now));
    }

    private Map<LocalDate, Long> loadDailyClicks(String code) {
        // Ordered query keeps analytics chart data stable for clients.
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT click_date, clicks FROM url_daily_clicks WHERE code = ? ORDER BY click_date",
                code
        );

        Map<LocalDate, Long> dailyClicks = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            Object clickDateValue = row.get("click_date");
            LocalDate day = clickDateValue instanceof Date
                    ? ((Date) clickDateValue).toLocalDate()
                    : (LocalDate) clickDateValue;
            Number clicks = (Number) row.get("clicks");
            dailyClicks.put(day, clicks.longValue());
        }

        return dailyClicks;
    }

    private static Timestamp toTimestamp(Instant instant) {
        // Centralized null-safe conversion for nullable DB timestamps.
        return instant == null ? null : Timestamp.from(instant);
    }

    private static Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }
}


