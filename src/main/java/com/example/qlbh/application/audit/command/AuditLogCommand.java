package com.example.qlbh.application.audit.command;

import com.example.qlbh.domain.audit.model.AuditAction;
import com.example.qlbh.domain.audit.model.ActorType;
import com.example.qlbh.domain.audit.model.AuditStatus;
import lombok.Builder;
import lombok.Getter;

/**
 * AuditLogCommand — command gửi vào Application layer để tạo audit log.
 * <p>
 * Được tạo bởi:
 * 1. AuditAspect (tự động, từ @AuditLog annotation)
 * 2. Code thủ công trong service khi cần log chi tiết hơn annotation cho phép
 */
@Getter
@Builder
public class AuditLogCommand {

  private String actorId;
  private String actorName;

  @Builder.Default
  private ActorType actorType = ActorType.USER;

  private AuditAction action;
  private String entityType;
  private String entityId;
  private String description;

  @Builder.Default
  private AuditStatus status = AuditStatus.SUCCESS;

  private String errorMessage;
  private String requestPayload;
  private String ipAddress;
  private String userAgent;
  private Long durationMs;
}
