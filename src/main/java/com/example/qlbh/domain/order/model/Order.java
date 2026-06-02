package com.example.qlbh.domain.order.model;

import com.example.qlbh.common.enums.OrderStatus;
import com.example.qlbh.common.exception.BusinessException;
import com.example.qlbh.domain.order.valueobject.Money;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

// domain/order/model/Order.java
@Getter
public class Order {

  private String id;
  private String customerId;
  private OrderStatus status;
  private List<OrderItem> items;
  private Money totalAmount;
  private Instant createdAt;

  /**
   * Constructor tạo Order mới — luôn bắt đầu ở DRAFT.
   */
  public Order(String customerId) {
    if (customerId == null || customerId.isBlank()) {
      throw new BusinessException("CustomerId không được trống");
    }
    this.customerId = customerId;
    this.status = OrderStatus.DRAFT;
    this.items = new ArrayList<>();
    this.totalAmount = Money.ZERO;
    this.createdAt = Instant.now();
    System.out.println("Order created with  this.createdAt: " + this.createdAt);
  }


  // Reconstitute từ DB
  public Order(
      String id,
      String customerId,
      OrderStatus status,
      List<OrderItem> items,
      Money totalAmount,
      Instant createdAt
  ) {
    this.id = id;
    this.customerId = customerId;
    this.status = status;
    this.items = new ArrayList<>(items);
    this.totalAmount = totalAmount;
    this.createdAt = createdAt;
  }

  /**
   * Thêm sản phẩm vào Order. Chỉ được thêm khi Order ở trạng thái DRAFT.
   * <p>
   * Invariant: Order không phải DRAFT → không thêm được
   */
  public void addItem(
      String productId,
      String productName,
      int quantity,
      Money unitPrice
  ) {
    validateStatus(OrderStatus.DRAFT,
        "Chỉ có thể thêm sản phẩm vào đơn hàng đang ở trạng thái DRAFT");

    // Nếu đã có product này → tăng quantity
    items.stream()
        .filter(item -> item.getProductId().equals(productId))
        .findFirst()
        .ifPresentOrElse(
            existing -> {
              // Tạo item mới với quantity tăng — immutable pattern
              items.remove(existing);
              items.add(new OrderItem(
                  existing.getId(),
                  productId,
                  productName,
                  existing.getQuantity() + quantity,
                  unitPrice
              ));
            },
            () -> items.add(new OrderItem(
                productId, productName, quantity, unitPrice
            ))
        );

    recalculateTotal();
  }

  /**
   * Confirm Order — chuyển từ DRAFT → CONFIRMED.
   * <p>
   * Invariants:
   * - Phải có ít nhất 1 item
   * - Chỉ DRAFT mới confirm được
   */
  public void confirm() {
    validateStatus(OrderStatus.DRAFT,
        "Chỉ có thể confirm đơn hàng ở trạng thái DRAFT");

    if (items.isEmpty()) {
      throw new BusinessException(
          "Không thể confirm đơn hàng không có sản phẩm"
      );
    }

    this.status = OrderStatus.CONFIRMED;
  }

  /**
   * Cancel Order.
   * <p>
   * DRAFT     → CANCELLED: được, không cần hoàn stock CONFIRMED → CANCELLED: được, phải hoàn stock (do caller xử lý)
   * SHIPPED   → CANCELLED: không được DELIVERED → CANCELLED: không được
   */
  public boolean cancel() {
    if (status == OrderStatus.SHIPPED
        || status == OrderStatus.DELIVERED) {
      throw new BusinessException(
          "Không thể hủy đơn hàng đang giao hoặc đã giao"
      );
    }

    boolean wasConfirmed = (status == OrderStatus.CONFIRMED);
    this.status = OrderStatus.CANCELLED;

    // Trả về true nếu cần hoàn stock
    return wasConfirmed;
  }

  /**
   * Ship Order — CONFIRMED → SHIPPED. Chỉ ADMIN/STAFF gọi được (kiểm tra ở Application layer).
   */
  public void ship() {
    validateStatus(OrderStatus.CONFIRMED,
        "Chỉ có thể ship đơn hàng ở trạng thái CONFIRMED");
    this.status = OrderStatus.SHIPPED;
  }

  /**
   * Deliver Order — SHIPPED → DELIVERED.
   */
  public void deliver() {
    validateStatus(OrderStatus.SHIPPED,
        "Chỉ có thể giao đơn hàng ở trạng thái SHIPPED");
    this.status = OrderStatus.DELIVERED;
  }

  /**
   * Kiểm tra order có thuộc về customer này không. Dùng để ngăn customer xem order của người khác.
   */
  public boolean belongsTo(String customerId) {
    return this.customerId.equals(customerId);
  }

  // ===== Private helpers =====

  private void validateStatus(
      OrderStatus expected,
      String errorMessage
  ) {
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