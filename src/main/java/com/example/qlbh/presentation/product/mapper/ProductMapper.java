package com.example.qlbh.presentation.product.mapper;

import com.example.qlbh.application.product.dto.ProductDto;
import com.example.qlbh.domain.product.model.Price;
import com.example.qlbh.domain.product.model.Product;
import com.example.qlbh.domain.product.valueobject.Stock;
import com.example.qlbh.presentation.product.response.ProductResponse;

public class ProductMapper {
  /**
   * Reconstitute Domain Object từ DB data.
   * Dùng constructor có id — object này đã tồn tại trong hệ thống.
   * Wrap primitive thành Value Object để có behavior và validation.
   */
  public Product toDomain(ProductDto productDto) {
    return new Product(
        productDto.getId(),        // id có — reconstitute từ DB
        productDto.getName(),
        productDto.getDescription(),
        productDto.getCategory(),
        new Price(productDto.getPrice()),   // BigDecimal → Value Object
        new Stock(productDto.getStock())    // int → Value Object
    );
  }

  /**
   * Chuyển Domain Object thành JPA Entity để persist.
   * Unwrap Value Object lấy primitive value để lưu DB.
   */
  public ProductDto toEntity(Product domain) {
    return new ProductDto(
        domain.getId(),
        domain.getName(),
        domain.getDescription(),
        domain.getCategory(),
        domain.getPrice().getValue(),
        domain.getStock().getQuantity()
    );
  }

  public ProductResponse  toResponse(ProductDto dto) {
    return new ProductResponse(
        dto.getId(),
        dto.getName(),
        dto.getDescription(),
        dto.getCategory(),
        dto.getPrice(),
        dto.getStock()
    );
  }
}
