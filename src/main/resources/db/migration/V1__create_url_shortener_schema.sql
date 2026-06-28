CREATE TABLE short_urls (
    code VARCHAR(20) PRIMARY KEY,
    original_url VARCHAR(2048) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NULL,
    click_count BIGINT NOT NULL DEFAULT 0,
    last_accessed_at TIMESTAMP NULL
);

CREATE TABLE url_daily_clicks (
    code VARCHAR(20) NOT NULL,
    click_date DATE NOT NULL,
    clicks BIGINT NOT NULL,
    PRIMARY KEY (code, click_date),
    CONSTRAINT fk_url_daily_clicks_short_url FOREIGN KEY (code) REFERENCES short_urls (code) ON DELETE CASCADE
);

CREATE INDEX idx_short_urls_expires_at ON short_urls (expires_at);

