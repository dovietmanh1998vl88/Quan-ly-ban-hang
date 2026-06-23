package com.example.qlbh.infrastructure.persistence.audit.entity;

import com.example.qlbh.domain.audit.model.AuditAction;
import com.example.qlbh.domain.audit.model.ActorType;
import com.example.qlbh.domain.audit.model.AuditStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/**
 * AuditLogEntity — JPA entity cho bảng audit_logs.
 * <p>
 * Không extend BaseEntity vì:
 * 1. AuditLog tự manage id (UUID v7 trong domain)
 * 2. AuditLog không cần updatedAt (immutable)
 * 3. createdAt = occurredAt (đặt tên rõ nghĩa hơn)
 * <p>
 * Indexes được thiết kế để tối ưu các query phổ biến:
 * - Filter theo actor (ai đã làm gì)
 * - Filter theo entity (ai đã làm gì với object này)
 * - Filter theo time range (audit trong tháng X)
 * - Filter theo action type
 */
@Getter
@Setter
@Entity
@Table(
    name = "audit_logs",
    indexes = {
        @Index(name = "idx_audit_actor_id", columnList = "actor_id"),
        @Index(name = "idx_audit_entity", columnList = "entity_type, entity_id"),
        @Index(name = "idx_audit_action", columnList = "action"),
        @Index(name = "idx_audit_occurred_at", columnList = "occurred_at"),
        @Index(name = "idx_audit_status", columnList = "status")
    }
)
public class AuditLogEntity {

  @Id
  @Column(name = "id", columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
  private String id;

  // ===== WHO =====
  @Column(name = "actor_id", nullable = false, length = 100)
  private String actorId;

  @Column(name = "actor_name", nullable = false, length = 255)
  private String actorName;

  @Enumerated(EnumType.STRING)
  @Column(name = "actor_type", nullable = false, length = 20)
  private ActorType actorType;

  // ===== WHAT =====
  @Enumerated(EnumType.STRING)
  @Column(name = "action", nullable = false, length = 50)
  private AuditAction action;

  @Column(name = "entity_type", length = 100)
  private String entityType;

  @Column(name = "entity_id", length = 100)
  private String entityId;

  @Column(name = "description", length = 500)
  private String description;

  // ===== RESULT =====
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private AuditStatus status;

  @Column(name = "error_message", length = 1000)
  private String errorMessage;

  // ===== CONTEXT =====
  @Column(name = "request_payload", columnDefinition = "TEXT")
  private String requestPayload;

  @Column(name = "ip_address", length = 50)
  private String ipAddress;

  @Column(name = "user_agent", length = 500)
  private String userAgent;

  // ===== WHEN =====
  @Column(name = "occurred_at", nullable = false, updatable = false)
  private Instant occurredAt;

  @Column(name = "duration_ms")
  private Long durationMs;
}