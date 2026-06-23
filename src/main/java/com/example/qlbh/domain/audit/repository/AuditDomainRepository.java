package com.example.qlbh.domain.audit.repository;

import com.example.qlbh.domain.audit.model.AuditAction;
import com.example.qlbh.domain.audit.model.AuditLog;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * AuditDomainRepository — domain interface.
 * <p>
 * Lý do audit log có method search phong phú hơn các repo khác: Business cần query audit theo nhiều chiều (ai, khi nào,
 * làm gì với cái gì) cho mục đích compliance, debugging, và security review.
 */
public interface AuditDomainRepository {

  AuditLog save(AuditLog auditLog);

  Optional<AuditLog> findById(String id);

  /**
   * Lấy toàn bộ audit của một entity cụ thể (ví dụ: lịch sử sản phẩm X)
   */
  List<AuditLog> findByEntityTypeAndEntityId(String entityType, String entityId);

  /**
   * Lấy toàn bộ audit của một actor (ví dụ: user X đã làm gì)
   */
  List<AuditLog> findByActorId(String actorId);

  /**
   * Filter theo action type (ví dụ: toàn bộ PRODUCT_DELETE trong tháng)
   */
  List<AuditLog> findByActionAndDateRange(AuditAction action, Instant from, Instant to);

  /**
   * Tìm kiếm audit log cho trang admin
   */
  List<AuditLog> search(
      String actorId,
      String entityType,
      AuditAction action,
      Instant from,
      Instant to,
      int page,
      int size
  );

  long countSearch(
      String actorId,
      String entityType,
      AuditAction action,
      Instant from,
      Instant to
  );
}