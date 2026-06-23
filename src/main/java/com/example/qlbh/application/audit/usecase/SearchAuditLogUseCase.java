package com.example.qlbh.application.audit.usecase;

import com.example.qlbh.application.audit.dto.AuditLogDto;
import com.example.qlbh.common.response.PageResponse;
import com.example.qlbh.domain.audit.model.AuditAction;
import java.time.Instant;

public interface SearchAuditLogUseCase {

  PageResponse<AuditLogDto> execute(
      String actorId,
      String entityType,
      AuditAction action,
      Instant from,
      Instant to,
      int page,
      int size
  );
}