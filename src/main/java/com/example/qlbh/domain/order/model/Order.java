package com.example.qlbh.domain.order.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.qlbh.common.enums.OrderStatus;
import com.example.qlbh.common.exception.BusinessException;
import com.example.qlbh.domain.AggregateRoot;
import com.example.qlbh.domain.order.event.OrderConfirmedEvent;
import com.example.qlbh.domain.order.valueobject.Money;
import com.example.qlbh.domain.order.valueobject.OrderCode;

import lombok.Getter;

@Getter
public class Order extends AggregateRoot {

  private String id;
  private String customerId;
  private OrderStatus status;
  private List<OrderItem> items;
  private Money totalAmount;
  private Instant createdAt;
  private OrderCode orderCode;

  public Order(String customerId) {
    if (customerId == null || customerId.isBlank()) {
      throw new BusinessException("CustomerId không được trống");
    }
    this.id = UUID.randomUUID().toString();
    this.customerId = customerId;
    this.status = OrderStatus.DRAFT;
    this.items = new ArrayList<>();
    this.totalAmount = Money.ZERO;
    this.createdAt = Instant.now();
  }

  public Order(
      String id,
      String customerId,
      OrderStatus status,
      List<OrderItem> items,
      Money totalAmount,
      Instant createdAt,
      OrderCode orderCode) {
    this.id = id;
    this.customerId = customerId;
    this.status = status;
    this.items = new ArrayList<>(items);
    this.totalAmount = totalAmount;
    this.createdAt = createdAt;
    this.orderCode = orderCode;
  }

  public void addItem(
      String productId,
      String productName,
      int quantity,
      Money unitPrice) {
    validateStatus(OrderStatus.DRAFT,
        "Chỉ có thể thêm sản phẩm vào đơn hàng đang ở trạng thái DRAFT");

    items.stream()
        .filter(item -> item.getProductId().equals(productId))
        .findFirst()
        .ifPresentOrElse(
            existing -> {
              items.remove(existing);
              items.add(new OrderItem(
                  existing.getId(),
                  productId,
                  productName,
                  existing.getQuantity() + quantity,
                  unitPrice));
            },
            () -> items.add(new OrderItem(
                UUID.randomUUID().toString(),
                productId,
                productName,
                quantity,
                unitPrice)));

    recalculateTotal();
  }

  public void addOrderCode(OrderCode orderCode) {
    validateStatus(OrderStatus.CONFIRMED,
        "Chỉ có thể hoàn thành khi đơn hàng ở trạng thái CONFIRMED");
    this.orderCode = orderCode;
  }

  public void confirm() {
    validateStatus(OrderStatus.DRAFT,
        "Chỉ có thể confirm đơn hàng ở trạng thái DRAFT");

    if (items.isEmpty()) {
      throw new BusinessException(
          "Không thể confirm đơn hàng không có sản phẩm");
    }

    this.status = OrderStatus.CONFIRMED;

    // Register domain event
    registerEvent(new OrderConfirmedEvent(
        this.id,
        this.customerId,
        items.stream()
            .map(item -> new OrderConfirmedEvent.OrderItemSnapshot(
                item.getProductId(),
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice().toString()))
            .toList(),
        this.totalAmount.toString()));
  }

  public boolean cancel() {
    if (status == OrderStatus.SHIPPED
        || status == OrderStatus.DELIVERED) {
      throw new BusinessException(
          "Không thể hủy đơn hàng đang giao hoặc đã giao");
    }

    boolean wasConfirmed = (status == OrderStatus.CONFIRMED);
    this.status = OrderStatus.CANCELLED;
    return wasConfirmed;
  }

  public void ship() {
    validateStatus(OrderStatus.CONFIRMED,
        "Chỉ có thể ship đơn hàng ở trạng thái CONFIRMED");
    this.status = OrderStatus.SHIPPED;
  }

  public void deliver() {
    validateStatus(OrderStatus.SHIPPED,
        "Chỉ có thể giao đơn hàng ở trạng thái SHIPPED");
    this.status = OrderStatus.DELIVERED;
  }

  public boolean belongsTo(String customerId) {
    return this.customerId.equals(customerId);
  }

  private void validateStatus(
      OrderStatus expected,
      String errorMessage) {
    if (this.status != expected) {
      throw new BusinessException(errorMessage);
    }
  }

  private void recalculateTotal() {
    this.totalAmount = items.stream()
        .map(OrderItem::subtotal)
        .reduce(Money.ZERO, Money::add);
  }
}
