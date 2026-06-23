package com.example.qlbh.infrastructure.outbox;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "outbox_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String eventType;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;

    @Column(nullable = false)
    private String aggregateId;

    @Column(nullable = false)
    private String aggregateType;

    @Column(nullable = false)
    private boolean processed = false;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant processedAt;

    private int retryCount = 0;

    public OutboxEntity(String eventType, String payload, String aggregateId, String aggregateType) {
        this.id = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.payload = payload;
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.createdAt = Instant.now();
    }

    public void markProcessed() {
        this.processed = true;
        this.processedAt = Instant.now();
    }

    public void incrementRetry() {
        this.retryCount++;
    }
}
