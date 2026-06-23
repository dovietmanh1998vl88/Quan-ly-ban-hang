package com.example.qlbh.infrastructure.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.qlbh.domain.audit.event.AuditLogCreatedEvent;
import com.example.qlbh.domain.audit.model.AuditLog;
import com.example.qlbh.domain.audit.repository.AuditDomainRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * AuditLogKafkaConsumer
 *
 * Listens to Kafka topic: qlbh.audit.log.created
 * Persists audit logs asynchronously.
 *
 * Decoupled from main business transaction flow.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditLogKafkaConsumer {

  private final AuditDomainRepository auditRepository;
  private final ObjectMapper objectMapper;

  @KafkaListener(topics = "qlbh.audit.log.created", groupId = "audit-processor-group", containerFactory = "kafkaListenerContainerFactory")
  @Transactional
  public void consumeAuditLog(String payload) {
    log.debug("Processing audit log from Kafka: {}", payload);

    try {
      AuditLogCreatedEvent event = objectMapper.readValue(
          payload,
          AuditLogCreatedEvent.class);

      AuditLog auditLog = event.getAuditLog();

      // Persist to database
      auditRepository.save(auditLog);

      log.info("Audit log persisted: action={}, actor={}, entity={}/{}",
          auditLog.getAction(),
          auditLog.getActorName(),
          auditLog.getEntityType(),
          auditLog.getEntityId());

      // TODO: Forward to ELK, Datadog, etc.
      // TODO: Check for security alerts

    } catch (Exception e) {
      log.error("Failed to process audit log", e);
      // In production, send to DLQ (Dead Letter Queue)
      throw new AuditProcessingException("Failed to process audit log", e);
    }
  }

  public static class AuditProcessingException extends RuntimeException {
    public AuditProcessingException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
