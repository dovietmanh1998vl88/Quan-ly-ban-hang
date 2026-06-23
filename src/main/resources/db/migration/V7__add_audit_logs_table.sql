-- V7__add_audit_logs_table.sql
-- Bảng audit_logs — lưu toàn bộ lịch sử hoạt động hệ thống.
--
-- Thiết kế:
-- - id: UUID v7 (time-ordered) → insert tuần tự, index không bị phân mảnh
-- - occurred_at: Instant (UTC) → dùng cho time-range filter
-- - request_payload: TEXT → nullable, chỉ log khi cần debug
-- - Tất cả lookup columns đều có index
-- - Không có FK → audit tồn tại độc lập dù entity bị xóa (compliance)

CREATE TABLE audit_logs (
    id              VARCHAR(36)   NOT NULL,
    actor_id        VARCHAR(100)  NOT NULL,
    actor_name      VARCHAR(255)  NOT NULL,
    actor_type      VARCHAR(20)   NOT NULL DEFAULT 'USER',
    action          VARCHAR(50)   NOT NULL,
    entity_type     VARCHAR(100),
    entity_id       VARCHAR(100),
    description     VARCHAR(500),
    status          VARCHAR(20)   NOT NULL DEFAULT 'SUCCESS',
    error_message   VARCHAR(1000),
    request_payload TEXT,
    ip_address      VARCHAR(50),
    user_agent      VARCHAR(500),
    occurred_at     DATETIME(6)   NOT NULL,
    duration_ms     BIGINT,

    PRIMARY KEY (id)
);

-- Index phục vụ các query phổ biến
CREATE INDEX idx_audit_actor_id    ON audit_logs (actor_id);
CREATE INDEX idx_audit_entity      ON audit_logs (entity_type, entity_id);
CREATE INDEX idx_audit_action      ON audit_logs (action);
CREATE INDEX idx_audit_occurred_at ON audit_logs (occurred_at);
CREATE INDEX idx_audit_status      ON audit_logs (status);