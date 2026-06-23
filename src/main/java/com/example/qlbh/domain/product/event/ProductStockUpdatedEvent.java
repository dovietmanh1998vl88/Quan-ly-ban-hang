package com.example.qlbh.domain.product.event;

import com.example.qlbh.domain.DomainEvent;
import java.time.Instant;

public record ProductStockUpdatedEvent(
    String productId,
    Integer oldStock,
    Integer newStock,
    Integer amount,
    String action,
    String userId,
    Instant occurredOn
) implements DomainEvent {

}
