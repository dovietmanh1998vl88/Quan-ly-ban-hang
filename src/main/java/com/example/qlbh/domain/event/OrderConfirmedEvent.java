package com.example.qlbh.domain.event;

public record OrderConfirmedEvent(
        String orderId,
        String customerId
// List<OrderItemSnapshot> items,
// Instant occurredAt
) {
}
