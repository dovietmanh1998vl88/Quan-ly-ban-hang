package com.example.qlbh.domain.audit.event;

import com.example.qlbh.domain.DomainEvent;
import com.example.qlbh.domain.audit.model.AuditLog;
import java.time.Instant;
import lombok.Getter;

/**
 * AuditLogCreatedEvent — Domain Event.
 * <p>
 * Được publish sau khi AuditLog được tạo thành công. Subscriber (infrastructure) có thể:
 * - Ghi vào DB bất đồng bộ (@Async)
 * - Gửi alert khi phát hiện suspicious activity
 * - Forward đến monitoring system (ELK, Datadog)
 * <p>
 * Domain event giúp audit không block main business flow.
 */
@Getter
public class AuditLogCreatedEvent implements DomainEvent {

  private final AuditLog auditLog;
  private final Instant publishedAt;

  public AuditLogCreatedEvent(AuditLog auditLog) {
    this.auditLog = auditLog;
    this.publishedAt = Instant.now();
  }
}