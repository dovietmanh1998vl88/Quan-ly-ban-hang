package com.example.qlbh.domain.order.valueobject;

import com.example.qlbh.common.exception.BusinessException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Money {

  public static final Money ZERO = new Money(BigDecimal.ZERO);

  private final BigDecimal amount;

  public Money(BigDecimal amount) {
    if (amount == null) {
      throw new BusinessException("Số tiền không được null");
    }
    if (amount.compareTo(BigDecimal.ZERO) < 0) {
      throw new BusinessException("Số tiền không được âm");
    }
    this.amount = amount.setScale(2, RoundingMode.HALF_UP);
  }

  public BigDecimal getAmount() {
    return amount;
  }

  // Immutable — trả về object mới
  public Money add(Money other) {
    return new Money(this.amount.add(other.amount));
  }

  public Money multiply(int quantity) {
    if (quantity <= 0) {
      throw new BusinessException("Số lượng phải lớn hơn 0");
    }
    return new Money(this.amount.multiply(BigDecimal.valueOf(quantity)));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Money)) {
      return false;
    }
    return amount.compareTo(((Money) o).amount) == 0;
  }

  @Override
  public int hashCode() {
    return amount.stripTrailingZeros().hashCode();
  }
}