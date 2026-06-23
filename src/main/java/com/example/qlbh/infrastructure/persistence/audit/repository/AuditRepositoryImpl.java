package com.example.qlbh.infrastructure.persistence.audit.repository;

import com.example.qlbh.domain.audit.model.AuditAction;
import com.example.qlbh.domain.audit.model.AuditLog;
import com.example.qlbh.domain.audit.repository.AuditDomainRepository;
import com.example.qlbh.infrastructure.persistence.audit.mapper.AuditPersistenceMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AuditRepositoryImpl implements AuditDomainRepository {

  private final AuditJpaRepository jpaRepository;
  private final AuditPersistenceMapper mapper;

  @Override
  public AuditLog save(AuditLog auditLog) {
    return mapper.toDomain(
        jpaRepository.save(mapper.toEntity(auditLog))
    );
  }

  @Override
  public Optional<AuditLog> findById(String id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public List<AuditLog> findByEntityTypeAndEntityId(String entityType, String entityId) {
    return jpaRepository
        .findByEntityTypeAndEntityIdOrderByOccurredAtDesc(entityType, entityId)
        .stream().map(mapper::toDomain).toList();
  }

  @Override
  public List<AuditLog> findByActorId(String actorId) {
    return jpaRepository
        .findByActorIdOrderByOccurredAtDesc(actorId)
        .stream().map(mapper::toDomain).toList();
  }

  @Override
  public List<AuditLog> findByActionAndDateRange(AuditAction action, Instant from, Instant to) {
    return jpaRepository
        .findByActionAndOccurredAtBetweenOrderByOccurredAtDesc(action, from, to)
        .stream().map(mapper::toDomain).toList();
  }

  @Override
  public List<AuditLog> search(
      String actorId,
      String entityType,
      AuditAction action,
      Instant from,
      Instant to,
      int page,
      int size
  ) {
    return jpaRepository
        .search(actorId, entityType, action, from, to,
            PageRequest.of(Math.max(page - 1, 0), size))
        .stream().map(mapper::toDomain).toList();
  }

  @Override
  public long countSearch(
      String actorId,
      String entityType,
      AuditAction action,
      Instant from,
      Instant to
  ) {
    return jpaRepository.countSearch(actorId, entityType, action, from, to);
  }
}