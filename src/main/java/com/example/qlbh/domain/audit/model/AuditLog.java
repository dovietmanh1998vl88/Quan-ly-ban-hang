package com.example.qlbh.domain.audit;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuditLog {

  private final String id;

  private final String action;

  private final String entityType;

  private final String entityId;

  private final String userId;

  private final String description;

  private final Instant createdAt;
}
