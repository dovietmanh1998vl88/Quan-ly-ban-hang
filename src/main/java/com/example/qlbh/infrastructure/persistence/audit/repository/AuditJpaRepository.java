package com.example.qlbh.infrastructure.persistence.audit.repository;

import com.example.qlbh.domain.audit.model.AuditAction;
import com.example.qlbh.infrastructure.persistence.audit.entity.AuditLogEntity;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditJpaRepository
    extends JpaRepository<AuditLogEntity, String>,
    JpaSpecificationExecutor<AuditLogEntity> {

  List<AuditLogEntity> findByEntityTypeAndEntityIdOrderByOccurredAtDesc(
      String entityType,
      String entityId
  );

  List<AuditLogEntity> findByActorIdOrderByOccurredAtDesc(String actorId);

  List<AuditLogEntity> findByActionAndOccurredAtBetweenOrderByOccurredAtDesc(
      AuditAction action,
      Instant from,
      Instant to
  );

  /**
   * Dynamic search với nullable params. Dùng JPQL thay vì Specification để dễ đọc hơn với query đơn giản.
   */
  @Query("""
      SELECT a FROM AuditLogEntity a
      WHERE (:actorId IS NULL OR a.actorId = :actorId)
        AND (:entityType IS NULL OR a.entityType = :entityType)
        AND (:action IS NULL OR a.action = :action)
        AND (:from IS NULL OR a.occurredAt >= :from)
        AND (:to IS NULL OR a.occurredAt <= :to)
      ORDER BY a.occurredAt DESC
      """)
  List<AuditLogEntity> search(
      @Param("actorId") String actorId,
      @Param("entityType") String entityType,
      @Param("action") AuditAction action,
      @Param("from") Instant from,
      @Param("to") Instant to,
      Pageable pageable
  );

  @Query("""
      SELECT COUNT(a) FROM AuditLogEntity a
      WHERE (:actorId IS NULL OR a.actorId = :actorId)
        AND (:entityType IS NULL OR a.entityType = :entityType)
        AND (:action IS NULL OR a.action = :action)
        AND (:from IS NULL OR a.occurredAt >= :from)
        AND (:to IS NULL OR a.occurredAt <= :to)
      """)
  long countSearch(
      @Param("actorId") String actorId,
      @Param("entityType") String entityType,
      @Param("action") AuditAction action,
      @Param("from") Instant from,
      @Param("to") Instant to
  );
}