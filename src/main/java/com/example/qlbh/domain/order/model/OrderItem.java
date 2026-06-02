package com.example.qlbh.domain.order.model;

import com.example.qlbh.common.exception.BusinessException;
import com.example.qlbh.domain.order.valueobject.Money;
import lombok.Getter;

// domain/order/model/OrderItem.java
@Getter
public class OrderItem {

  private String id;
  private String productId;
  private String productName;  // snapshot
  private int quantity;
  private Money unitPrice;     // snapshot

  /**
   * Constructor tạo mới OrderItem. Snapshot productName và unitPrice tại thời điểm đặt hàng.
   */
  public OrderItem(
      String productId,
      String productName,
      int quantity,
      Money unitPrice
  ) {
    if (productId == null || productId.isBlank()) {
      throw new BusinessException("ProductId không được trống");
    }
    if (productName == null || productName.isBlank()) {
      throw new BusinessException("ProductName không được trống");
    }
    if (quantity <= 0) {
      throw new BusinessException("Số lượng phải lớn hơn 0");
    }
    this.productId = productId;
    this.productName = productName;
    this.quantity = quantity;
    this.unitPrice = unitPrice;
  }

  // Reconstitute từ DB
  public OrderItem(
      String id,
      String productId,
      String productName,
      int quantity,
      Money unitPrice
  ) {
    this(productId, productName, quantity, unitPrice);
    this.id = id;
  }

  /**
   * Tính tổng tiền của item này. unitPrice × quantity
   */
  public Money subtotal() {
    return unitPrice.multiply(quantity);
  }
}
