package com.example.qlbh.application.audit.mapper;

import com.example.qlbh.application.audit.command.AuditLogCommand;
import com.example.qlbh.application.audit.dto.AuditLogDto;
import com.example.qlbh.domain.audit.model.AuditLog;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

@Component
public class AuditApplicationMapper {

  private static final DateTimeFormatter FORMATTER =
      DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
          .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

  public AuditLog toDomain(AuditLogCommand command) {
    return AuditLog.builder()
        .actorId(command.getActorId())
        .actorName(command.getActorName())
        .actorType(command.getActorType())
        .action(command.getAction())
        .entityType(command.getEntityType())
        .entityId(command.getEntityId())
        .description(command.getDescription())
        .status(command.getStatus())
        .errorMessage(command.getErrorMessage())
        .requestPayload(command.getRequestPayload())
        .ipAddress(command.getIpAddress())
        .userAgent(command.getUserAgent())
        .durationMs(command.getDurationMs())
        .build();
  }

  public AuditLogDto toDto(AuditLog log) {
    return AuditLogDto.builder()
        .id(log.getId())
        .actorId(log.getActorId())
        .actorName(log.getActorName())
        .actorType(log.getActorType())
        .action(log.getAction())
        .entityType(log.getEntityType())
        .entityId(log.getEntityId())
        .description(log.getDescription())
        .status(log.getStatus())
        .errorMessage(log.getErrorMessage())
        .ipAddress(log.getIpAddress())
        .occurredAt(log.getOccurredAt() != null
            ? FORMATTER.format(log.getOccurredAt())
            : null)
        .durationMs(log.getDurationMs())
        .build();
  }
}