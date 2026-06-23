package com.example.qlbh.domain.order.event;

import java.time.Instant;
import java.util.List;

import com.example.qlbh.domain.DomainEvent;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderConfirmedEvent implements DomainEvent {

  private String orderId;
  private String customerId;
  private List<OrderItemSnapshot> items;
  private String totalAmount;
  private Instant occurredAt;

  public OrderConfirmedEvent(
      String orderId,
      String customerId,
      List<OrderItemSnapshot> items,
      String totalAmount) {
    this.orderId = orderId;
    this.customerId = customerId;
    this.items = items;
    this.totalAmount = totalAmount;
    this.occurredAt = Instant.now();
  }

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class OrderItemSnapshot {
    private String productId;
    private String productName;
    private Integer quantity;
    private String unitPrice;
  }
}
