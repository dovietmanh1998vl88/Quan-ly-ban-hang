package com.example.qlbh.infrastructure.scheduler;

import java.util.List;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.qlbh.infrastructure.outbox.OutboxEntity;
import com.example.qlbh.infrastructure.outbox.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPoller {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String KAFKA_TOPIC_PREFIX = "qlbh.";
    private static final int MAX_RETRIES = 3;

    /**
     * Poll outbox table every 1 second and publish unprocessed events to Kafka.
     *
     * This is the bridge between transactional DB and eventual-consistency Kafka.
     */
    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void processOutbox() {
        List<OutboxEntity> unprocessedEvents = outboxRepository.findUnprocessed();

        for (OutboxEntity event : unprocessedEvents) {
            try {
                publishToKafka(event);
                event.markProcessed();
                outboxRepository.save(event);
                log.debug("Event processed and marked: id={}, type={}", event.getId(), event.getEventType());

            } catch (Exception e) {
                handleEventProcessingError(event, e);
            }
        }
    }

    private void publishToKafka(OutboxEntity event) throws Exception {
        String topicName = generateTopicName(event.getEventType());

        kafkaTemplate.send(
                topicName,
                event.getAggregateId(), // Key: for partitioning by aggregate
                event.getPayload() // Value: JSON payload
        ).get();

        log.info("Event published to Kafka: topic={}, eventId={}, type={}, aggregateId={}",
                topicName, event.getId(), event.getEventType(), event.getAggregateId());
    }

    private void handleEventProcessingError(OutboxEntity event, Exception e) {
        log.error("Failed to process outbox event: id={}, type={}, retries={}",
                event.getId(), event.getEventType(), event.getRetryCount(), e);

        if (event.getRetryCount() < MAX_RETRIES) {
            event.incrementRetry();
            outboxRepository.save(event);
            log.info("Event will be retried: id={}, nextRetry={}", event.getId(), event.getRetryCount());
        } else {
            log.error("Event exceeded max retries, marking as processed: id={}", event.getId());
            event.markProcessed();
            outboxRepository.save(event);
            // TODO: Send alert to ops team
        }
    }

    private String generateTopicName(String eventType) {
        // OrderConfirmedEvent → qlbh.order.confirmed
        return KAFKA_TOPIC_PREFIX + toKebabCase(eventType);
    }

    private String toKebabCase(String eventType) {
        // OrderConfirmedEvent → order.confirmed
        return eventType
                .replaceAll("([a-z])([A-Z])", "$1.$2")
                .replaceAll("Event$", "")
                .toLowerCase();
    }
}
