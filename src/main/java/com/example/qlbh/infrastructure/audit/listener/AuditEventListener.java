package com.example.qlbh.infrastructure.audit.listener;

import com.example.qlbh.domain.audit.event.AuditLogCreatedEvent;
import com.example.qlbh.domain.audit.model.AuditAction;
import com.example.qlbh.domain.audit.model.AuditLog;
import com.example.qlbh.domain.audit.model.AuditStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * AuditEventListener — xử lý AuditLogCreatedEvent bất đồng bộ.
 *
 * @Async đảm bảo listener chạy trên thread pool riêng, không block business request.
 * <p>
 * Đây là nơi tích hợp:
 * - Security alert (nhiều lần login thất bại)
 * - Metrics (Micrometer → Prometheus → Grafana)
 * - Notification (email, Slack khi ADMIN bị xóa)
 * - Forwarding sang ELK / Datadog / Splunk
 */
@Slf4j
@Component
public class AuditEventListener {

  @Async
  @EventListener
  public void onAuditLogCreated(AuditLogCreatedEvent event) {
    AuditLog auditLog = event.getAuditLog();

    // ===== Security: alert khi phát hiện suspicious activity =====
    if (auditLog.getAction() == AuditAction.AUTH_LOGIN_FAILED) {
      log.warn("[Security] LOGIN_FAILED — actor={}, ip={}",
          auditLog.getActorName(), auditLog.getIpAddress());
      // TODO: integrate với rate limiter / alerting service
    }

    // ===== Alert khi ADMIN bị tác động =====
    if (isHighRiskAction(auditLog)) {
      log.warn("[Security] HIGH_RISK action detected — action={}, actor={}, entity={}/{}",
          auditLog.getAction(),
          auditLog.getActorName(),
          auditLog.getEntityType(),
          auditLog.getEntityId());
      // TODO: gửi Slack alert / email alert
    }

    // ===== Performance monitoring =====
    if (auditLog.getDurationMs() != null && auditLog.getDurationMs() > 3000) {
      log.warn("[Performance] Slow operation — action={}, duration={}ms, actor={}",
          auditLog.getAction(),
          auditLog.getDurationMs(),
          auditLog.getActorName());
      // TODO: push metric vào Micrometer
    }

    // ===== Log failed actions với detail =====
    if (auditLog.getStatus() == AuditStatus.FAILED) {
      log.info("[Audit] FAILED — action={}, actor={}, error={}",
          auditLog.getAction(),
          auditLog.getActorName(),
          auditLog.getErrorMessage());
    }
  }

  private boolean isHighRiskAction(AuditLog log) {
    return log.getAction() == AuditAction.PRODUCT_DELETE
        || log.getAction() == AuditAction.USER_UPDATE
        || log.getAction() == AuditAction.SYSTEM_ACCESS_DENIED;
  }
}