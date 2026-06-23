package com.example.qlbh.infrastructure.persistence.audit.mapper;

import com.example.qlbh.domain.audit.model.AuditLog;
import com.example.qlbh.infrastructure.persistence.audit.entity.AuditLogEntity;
import org.springframework.stereotype.Component;

@Component
public class AuditPersistenceMapper {

  public AuditLogEntity toEntity(AuditLog domain) {
    AuditLogEntity entity = new AuditLogEntity();
    entity.setId(domain.getId());
    entity.setActorId(domain.getActorId());
    entity.setActorName(domain.getActorName());
    entity.setActorType(domain.getActorType());
    entity.setAction(domain.getAction());
    entity.setEntityType(domain.getEntityType());
    entity.setEntityId(domain.getEntityId());
    entity.setDescription(domain.getDescription());
    entity.setStatus(domain.getStatus());
    entity.setErrorMessage(domain.getErrorMessage());
    entity.setRequestPayload(domain.getRequestPayload());
    entity.setIpAddress(domain.getIpAddress());
    entity.setUserAgent(domain.getUserAgent());
    entity.setOccurredAt(domain.getOccurredAt());
    entity.setDurationMs(domain.getDurationMs());
    return entity;
  }

  public AuditLog toDomain(AuditLogEntity entity) {
    return new AuditLog(
        entity.getId(),
        entity.getActorId(),
        entity.getActorName(),
        entity.getActorType(),
        entity.getAction(),
        entity.getEntityType(),
        entity.getEntityId(),
        entity.getDescription(),
        entity.getStatus(),
        entity.getErrorMessage(),
        entity.getRequestPayload(),
        entity.getIpAddress(),
        entity.getUserAgent(),
        entity.getOccurredAt(),
        entity.getDurationMs()
    );
  }
}