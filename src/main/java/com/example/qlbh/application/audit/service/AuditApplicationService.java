package com.example.qlbh.application.audit.service;

import java.time.Instant;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.qlbh.application.audit.command.AuditLogCommand;
import com.example.qlbh.application.audit.dto.AuditLogDto;
import com.example.qlbh.application.audit.mapper.AuditApplicationMapper;
import com.example.qlbh.application.audit.usecase.CreateAuditLogUseCase;
import com.example.qlbh.application.audit.usecase.SearchAuditLogUseCase;
import com.example.qlbh.common.response.PageResponse;
import com.example.qlbh.domain.audit.event.AuditLogCreatedEvent;
import com.example.qlbh.domain.audit.model.AuditAction;
import com.example.qlbh.domain.audit.model.AuditLog;
import com.example.qlbh.domain.audit.repository.AuditDomainRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditApplicationService
    implements CreateAuditLogUseCase, SearchAuditLogUseCase {

  private final AuditDomainRepository auditRepository;
  private final AuditApplicationMapper mapper;
  private final ApplicationEventPublisher eventPublisher;

  /**
   * Tạo audit log.
   * <p>
   * Dùng Propagation.REQUIRES_NEW để audit luôn được ghi, kể cả khi transaction
   * cha bị rollback.
   * <p>
   * Ví dụ: User cố xóa product không tồn tại → NotFoundException được throw,
   * transaction chính rollback, nhưng audit
   * "PRODUCT_DELETE / FAILED" vẫn ghi được.
   * <p>
   * REQUIRES_NEW = tạo transaction riêng, tách hoàn toàn khỏi business
   * transaction.
   */
  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void execute(AuditLogCommand command) {
    try {
      AuditLog auditLog = mapper.toDomain(command);
      AuditLog saved = auditRepository.save(auditLog);

      // Publish domain event → listener có thể gửi alert, metrics, v.v.
      eventPublisher.publishEvent(new AuditLogCreatedEvent(saved));

      log.debug("[Audit] {} - {} - {} - {}",
          command.getAction(),
          command.getActorName(),
          command.getEntityType(),
          command.getStatus());

    } catch (Exception e) {
      // Audit KHÔNG được throw exception làm hỏng business flow
      // Chỉ log lỗi ở đây
      log.error("[Audit] Failed to save audit log for action={}: {}",
          command.getAction(), e.getMessage(), e);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponse<AuditLogDto> execute(
      String actorId,
      String entityType,
      AuditAction action,
      Instant from,
      Instant to,
      int page,
      int size) {
    List<AuditLog> logs = auditRepository.search(
        actorId, entityType, action, from, to, page, size);
    long total = auditRepository.countSearch(
        actorId, entityType, action, from, to);
    List<AuditLogDto> dtos = logs.stream()
        .map(mapper::toDto)
        .toList();

    return new PageResponse<>(dtos, page, size, total);
  }
}