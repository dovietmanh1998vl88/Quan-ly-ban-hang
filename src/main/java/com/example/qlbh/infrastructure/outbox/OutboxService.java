package com.example.qlbh.infrastructure.outbox;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.qlbh.domain.DomainEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxService {

  private final OutboxRepository outboxRepository;
  private final ObjectMapper objectMapper;

  /**
   * Publish domain events to outbox (same transaction).
   * Called inside @Transactional service.
   */
  @Transactional
  public void publishEventsToOutbox(
      List<DomainEvent> events,
      String aggregateId,
      String aggregateType) {
    for (DomainEvent event : events) {
      publishEventToOutbox(event, aggregateId, aggregateType);
    }
  }

  @Transactional
  public void publishEventToOutbox(
      DomainEvent event,
      String aggregateId,
      String aggregateType) {
    try {
      String eventType = event.getClass().getSimpleName();
      String payload = objectMapper.writeValueAsString(event);

      OutboxEntity outboxEntity = new OutboxEntity(
          eventType,
          payload,
          aggregateId,
          aggregateType);

      outboxRepository.save(outboxEntity);
      log.debug("Event saved to outbox: type={}, aggregateId={}", eventType, aggregateId);

    } catch (Exception e) {
      log.error("Failed to publish event to outbox", e);
      throw new RuntimeException("Failed to publish event to outbox", e);
    }
  }
}
