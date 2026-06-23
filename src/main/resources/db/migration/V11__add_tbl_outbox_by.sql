CREATE TABLE outbox_events (
    id VARCHAR(36) PRIMARY KEY,
    event_type VARCHAR(255) NOT NULL,
    payload LONGTEXT NOT NULL,
    aggregate_id VARCHAR(36) NOT NULL,
    aggregate_type VARCHAR(255) NOT NULL,
    processed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL,
    retry_count INT NOT NULL DEFAULT 0,
    INDEX idx_processed_created (processed, created_at),
    INDEX idx_aggregate (aggregate_id, aggregate_type)
);
