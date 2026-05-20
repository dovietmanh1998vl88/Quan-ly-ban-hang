package com.example.qlbh.domain.product.valueobject;

import com.example.qlbh.common.exception.BusinessException;

/**
 * Value Object đại diện cho số lượng tồn kho.
 *
 * Immutable — mọi thao tác tăng/giảm đều trả về Stock MỚI
 * thay vì sửa object hiện tại. Giống String trong Java:
 * "hello".toUpperCase() trả về "HELLO" mới, không sửa "hello".
 *
 * Lợi ích immutable:
 * - Thread-safe: nhiều thread đọc cùng lúc không conflict
 * - Dễ debug: biết chắc object không bị ai sửa ngầm
 * - Dễ test: không có side effect
 */
public final class Stock {

  private final int quantity;

  public Stock(int quantity) {
    if (quantity < 0) {
      throw new BusinessException("Số lượng tồn kho không được âm");
    }
    this.quantity = quantity;
  }

  public int getQuantity() {
    return quantity;
  }

  /**
   * Giảm tồn kho — trả về Stock MỚI, không sửa object hiện tại.
   *
   * Business rule được bảo vệ tại đây:
   * - Không cho giảm với amount <= 0
   * - Không cho giảm quá số lượng hiện có
   *
   * Nhờ vậy không bao giờ tồn tại Stock âm trong hệ thống.
   */
  public Stock decrease(int amount) {
    if (amount <= 0) {
      throw new BusinessException("Số lượng giảm phải lớn hơn 0");
    }
    if (this.quantity < amount) {
      throw new BusinessException("Tồn kho không đủ");
    }
    return new Stock(this.quantity - amount);  // trả về object mới
  }

  /**
   * Tăng tồn kho — trả về Stock MỚI.
   * Dùng khi nhập hàng hoặc hủy đơn hàng.
   */
  public Stock increase(int amount) {
    if (amount <= 0) {
      throw new BusinessException("Số lượng tăng phải lớn hơn 0");
    }
    return new Stock(this.quantity + amount);  // trả về object mới
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Stock)) return false;
    return quantity == ((Stock) o).quantity;
  }

  @Override
  public int hashCode() {
    return Integer.hashCode(quantity);
  }
}
