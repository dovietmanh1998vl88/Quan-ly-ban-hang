package com.example.qlbh.domain.product.model;

import com.example.qlbh.common.exception.BusinessException;
import java.math.BigDecimal;

/**
 * Value Object đại diện cho giá tiền.
 *
 * Value Object trong DDD có 3 đặc điểm:
 * 1. Không có identity (id) — phân biệt nhau bằng giá trị
 * 2. Immutable — không thay đổi sau khi tạo
 * 3. Self-validating — tự validate trong constructor
 *
 * Dùng BigDecimal thay vì double vì double có lỗi floating point:
 * 0.1 + 0.2 = 0.30000000000000004 → sai khi tính tiền
 */
public final class Price {  // final — không cho extend, giữ immutability

  private final BigDecimal value;  // final — không thay đổi sau khi gán

  /**
   * Constructor validate luôn khi tạo.
   * Không bao giờ tồn tại Price với giá trị không hợp lệ trong hệ thống.
   */
  public Price(BigDecimal value) {
    if (value == null) {
      throw new BusinessException("Giá không được null");
    }
    if (value.compareTo(BigDecimal.ZERO) < 0) {
      // compareTo thay vì < vì BigDecimal không dùng được toán tử
      throw new BusinessException("Giá không được âm");
    }
    this.value = value;
  }

  public BigDecimal getValue() {
    return value;
  }

  /**
   * Value Object so sánh bằng GIÁ TRỊ, không phải reference.
   * Price(100) == Price(100) → true
   * Khác Entity: User(id=1) == User(id=1) dù username khác nhau
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Price)) return false;
    Price price = (Price) o;
    // compareTo thay vì equals vì BigDecimal.equals("1.0") != BigDecimal.equals("1.00")
    return value.compareTo(price.value) == 0;
  }

  @Override
  public int hashCode() {
    // stripTrailingZeros để 1.0 và 1.00 có cùng hashCode
    return value.stripTrailingZeros().hashCode();
  }
}
