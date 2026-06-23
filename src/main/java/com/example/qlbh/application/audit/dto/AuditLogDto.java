package com.example.qlbh.application.audit.dto;

import com.example.qlbh.domain.audit.model.AuditAction;
import com.example.qlbh.domain.audit.model.ActorType;
import com.example.qlbh.domain.audit.model.AuditStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AuditLogDto {

  private String id;
  private String actorId;
  private String actorName;
  private ActorType actorType;
  private AuditAction action;
  private String entityType;
  private String entityId;
  private String description;
  private AuditStatus status;
  private String errorMessage;
  private String ipAddress;
  private String occurredAt;     // formatted string cho API response
  private Long durationMs;
}