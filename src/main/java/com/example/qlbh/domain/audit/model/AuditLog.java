package com.example.qlbh.domain.audit.model;

import com.example.qlbh.common.util.UuidGenerator;
import java.time.Instant;
import lombok.Getter;

/**
 * AuditLog — Aggregate Root của Audit bounded context.
 * <p>
 * Đây là immutable record: một audit log không bao giờ bị sửa sau khi tạo. Business rule: "Audit logs are write-once,
 * read-many."
 * <p>
 * Tại sao là Aggregate Root riêng? → Audit không phụ thuộc vào Product, Order hay User. → Nếu Product bị xóa, audit của
 * nó vẫn phải tồn tại (compliance requirement). → Lifecycle độc lập = Aggregate riêng.
 */
@Getter
public class AuditLog {

  private final String id;

  // ===== WHO =====
  /**
   * ID của actor thực hiện action (Keycloak sub / system)
   */
  private final String actorId;

  /**
   * Username hoặc service name
   */
  private final String actorName;

  private final ActorType actorType;

  // ===== WHAT =====
  private final AuditAction action;

  /**
   * Module/aggregate bị tác động: "Product", "Order", "User"
   */
  private final String entityType;

  /**
   * ID của entity bị tác động (có thể null với action tạo mới)
   */
  private final String entityId;

  /**
   * Mô tả ngắn gọn — "Tạo sản phẩm 'Áo thun trắng'"
   */
  private final String description;

  // ===== RESULT =====
  private final AuditStatus status;

  /**
   * Chi tiết lỗi khi status = FAILED
   */
  private final String errorMessage;

  // ===== CONTEXT =====
  /**
   * JSON snapshot của request (tùy chọn — không log sensitive data)
   */
  private final String requestPayload;

  /**
   * IP address của client
   */
  private final String ipAddress;

  /**
   * User-Agent header
   */
  private final String userAgent;

  // ===== WHEN =====
  private final Instant occurredAt;

  /**
   * Thời gian thực thi (ms) — hữu ích cho performance audit
   */
  private final Long durationMs;

  /**
   * Factory method — tạo AuditLog mới (write-once). Private constructor ngăn tạo object không hợp lệ từ bên ngoài.
   */
  private AuditLog(
      String actorId,
      String actorName,
      ActorType actorType,
      AuditAction action,
      String entityType,
      String entityId,
      String description,
      AuditStatus status,
      String errorMessage,
      String requestPayload,
      String ipAddress,
      String userAgent,
      Long durationMs
  ) {
    this.id = UuidGenerator.generateString();
    this.actorId = actorId;
    this.actorName = actorName;
    this.actorType = actorType;
    this.action = action;
    this.entityType = entityType;
    this.entityId = entityId;
    this.description = description;
    this.status = status;
    this.errorMessage = errorMessage;
    this.requestPayload = requestPayload;
    this.ipAddress = ipAddress;
    this.userAgent = userAgent;
    this.occurredAt = Instant.now();
    this.durationMs = durationMs;
  }

  /**
   * Reconstitute từ DB — dùng khi load từ persistence. Tất cả field được cung cấp, kể cả id và occurredAt.
   */
  public AuditLog(
      String id,
      String actorId,
      String actorName,
      ActorType actorType,
      AuditAction action,
      String entityType,
      String entityId,
      String description,
      AuditStatus status,
      String errorMessage,
      String requestPayload,
      String ipAddress,
      String userAgent,
      Instant occurredAt,
      Long durationMs
  ) {
    this.id = id;
    this.actorId = actorId;
    this.actorName = actorName;
    this.actorType = actorType;
    this.action = action;
    this.entityType = entityType;
    this.entityId = entityId;
    this.description = description;
    this.status = status;
    this.errorMessage = errorMessage;
    this.requestPayload = requestPayload;
    this.ipAddress = ipAddress;
    this.userAgent = userAgent;
    this.occurredAt = occurredAt;
    this.durationMs = durationMs;
  }

  // ===== Builder (nằm trong domain, không dùng Lombok @Builder để kiểm soát) =====

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private String actorId;
    private String actorName;
    private ActorType actorType = ActorType.USER;
    private AuditAction action;
    private String entityType;
    private String entityId;
    private String description;
    private AuditStatus status = AuditStatus.SUCCESS;
    private String errorMessage;
    private String requestPayload;
    private String ipAddress;
    private String userAgent;
    private Long durationMs;

    public Builder actorId(String actorId) {
      this.actorId = actorId;
      return this;
    }

    public Builder actorName(String actorName) {
      this.actorName = actorName;
      return this;
    }

    public Builder actorType(ActorType actorType) {
      this.actorType = actorType;
      return this;
    }

    public Builder action(AuditAction action) {
      this.action = action;
      return this;
    }

    public Builder entityType(String entityType) {
      this.entityType = entityType;
      return this;
    }

    public Builder entityId(String entityId) {
      this.entityId = entityId;
      return this;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder status(AuditStatus status) {
      this.status = status;
      return this;
    }

    public Builder errorMessage(String errorMessage) {
      this.errorMessage = errorMessage;
      return this;
    }

    public Builder requestPayload(String requestPayload) {
      this.requestPayload = requestPayload;
      return this;
    }

    public Builder ipAddress(String ipAddress) {
      this.ipAddress = ipAddress;
      return this;
    }

    public Builder userAgent(String userAgent) {
      this.userAgent = userAgent;
      return this;
    }

    public Builder durationMs(Long durationMs) {
      this.durationMs = durationMs;
      return this;
    }

    public AuditLog build() {
      validate();
      return new AuditLog(
          actorId, actorName, actorType,
          action, entityType, entityId,
          description, status, errorMessage,
          requestPayload, ipAddress, userAgent,
          durationMs
      );
    }

    private void validate() {
      if (action == null) {
        throw new IllegalStateException("AuditLog.action must not be null");
      }
      if (actorId == null || actorId.isBlank()) {
        throw new IllegalStateException("AuditLog.actorId must not be blank");
      }
    }
  }
}