package com.example.qlbh.domain.order.event;

import java.time.Instant;

import com.example.qlbh.domain.DomainEvent;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancelledEvent implements DomainEvent {

  private String orderId;
  private String customerId;
  private String reason;
  private boolean needsStockRollback;
  private Instant occurredAt;

  public OrderCancelledEvent(
      String orderId,
      String customerId,
      String reason,
      boolean needsStockRollback) {
    this.orderId = orderId;
    this.customerId = customerId;
    this.reason = reason;
    this.needsStockRollback = needsStockRollback;
    this.occurredAt = Instant.now();
  }
}
