package com.example.qlbh.domain.product.model;

import com.example.qlbh.common.exception.BusinessException;
import com.example.qlbh.domain.product.valueobject.Stock;
import lombok.Builder;
import lombok.Getter;

/**
 * Product Entity — đồng thời là Aggregate Root.
 *
 * Entity trong DDD:
 * - Có identity (id) — phân biệt nhau bằng id dù các field khác nhau
 * - Có thể thay đổi state theo thời gian
 * - Bảo vệ business rule qua các method
 *
 * Aggregate Root:
 * - Là cửa duy nhất để tương tác với Aggregate
 * - Đảm bảo invariant (bất biến) luôn được thỏa mãn
 * - Bên ngoài không được chọc thẳng vào Price/Stock
 *   mà phải gọi method của Product
 */
@Getter
@Builder
public class Product {

  private Long id;
  private String name;
  private String category;
  private String description;

  // Dùng Value Object thay vì primitive
  // → validation và behavior đóng gói trong chính nó
  private Price price;
  private Stock stock;

  /**
   * Constructor tạo MỚI — chưa có id.
   * Id sẽ do database sinh ra sau khi save.
   * Phân biệt với constructor reconstitute bên dưới.
   */
  public Product(
      String name,
      String description,
      String category,
      Price price,
      Stock stock
  ) {
    validateName(name);
    this.name = name;
    this.description = description;
    this.price = price;
    this.stock = stock;
    this.category = category;
    // id để null — DB sẽ tự sinh
  }

  /**
   * Constructor RECONSTITUTE — tái tạo từ dữ liệu DB, có id.
   *
   * Tại sao cần constructor riêng?
   * Khi load từ DB, object đã tồn tại → cần truyền id vào.
   * Khi tạo mới, chưa có id → không truyền.
   * Tách biệt 2 trường hợp giúp code rõ ràng, tránh nhầm lẫn.
   */
  public Product(
      Long id,
      String name,
      String description,
      String category,
      Price price,
      Stock stock
  ) {
    this(name, description,category , price, stock);  // gọi constructor trên
    this.id = id;
  }

  /**
   * Cập nhật giá — business method thay vì setter thô.
   * Tên method nói lên ý định, không chỉ "set value".
   */
  public void updatePrice(Price newPrice) {
    this.price = newPrice;
  }

  /**
   * Giảm tồn kho khi bán hàng.
   * Delegate validation xuống Stock Value Object
   * → Product không cần biết rule cụ thể, chỉ cần gọi
   */
  public void decreaseStock(int amount) {
    // Stock.decrease() tự validate và trả về Stock mới
    this.stock = this.stock.decrease(amount);
  }

  /**
   * Tăng tồn kho khi nhập hàng hoặc hủy đơn.
   */
  public void increaseStock(int amount) {
    this.stock = this.stock.increase(amount);
  }

  /**
   * Query method — hỏi trạng thái, không thay đổi state.
   * Dùng trong UI để hiển thị badge "Còn hàng / Hết hàng".
   */
  public boolean isInStock() {
    return this.stock.getQuantity() > 0;
  }

  /**
   * Private — chỉ dùng nội bộ trong class.
   * Validation tên tập trung một chỗ, tránh lặp ở 2 constructor.
   */
  private void validateName(String name) {
    if (name == null || name.isBlank()) {
      throw new BusinessException("Tên sản phẩm không được trống");
    }
    if (name.length() > 255) {
      throw new BusinessException("Tên sản phẩm tối đa 255 ký tự");
    }
  }
}