package com.example.qlbh.application.product.valueobject;

import com.example.qlbh.common.exception.BusinessException;
import com.example.qlbh.domain.product.valueobject.Stock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Stock Value Object")
class StockTest {

  @Test
  @DisplayName("Tạo Stock hợp lệ — thành công")
  void create_validQuantity_success() {
    Stock stock = new Stock(10);
    assertThat(stock.getQuantity()).isEqualTo(10);
  }

  @Test
  @DisplayName("Tạo Stock âm — throw BusinessException")
  void create_negativeQuantity_throwException() {
    assertThatThrownBy(() -> new Stock(-1))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("không được âm");
  }

  @Test
  @DisplayName("decrease() — trả về Stock MỚI, không sửa object cũ")
  void decrease_returnsNewObject_immutable() {
    Stock original = new Stock(10);
    Stock decreased = original.decrease(3);

    // Object cũ không thay đổi — immutability
    assertThat(original.getQuantity()).isEqualTo(10);
    // Object mới có giá trị đúng
    assertThat(decreased.getQuantity()).isEqualTo(7);
  }

  @Test
  @DisplayName("decrease() quá số lượng hiện có — throw BusinessException")
  void decrease_moreThanAvailable_throwException() {
    Stock stock = new Stock(5);

    assertThatThrownBy(() -> stock.decrease(10))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("Tồn kho không đủ");
  }

  @Test
  @DisplayName("increase() — trả về Stock MỚI")
  void increase_returnsNewObject() {
    Stock stock = new Stock(10);
    Stock increased = stock.increase(5);

    assertThat(stock.getQuantity()).isEqualTo(10); // cũ không đổi
    assertThat(increased.getQuantity()).isEqualTo(15);
  }
}
