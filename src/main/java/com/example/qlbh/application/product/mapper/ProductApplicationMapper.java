package com.example.qlbh.application.product.mapper;

import com.example.qlbh.application.product.command.CreateProductCommand;
import com.example.qlbh.application.product.dto.ProductDto;
import com.example.qlbh.domain.product.model.Price;
import com.example.qlbh.domain.product.model.Product;
import com.example.qlbh.domain.product.valueobject.Stock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductApplicationMapper {
  public Product toDomain(CreateProductCommand command) {
    return new Product(command.getName(), command.getDescription(), command.getCategory(), new Price(command.getPrice()), new Stock(command.getStock()));
  }
  public ProductDto toDto(Product product) {
    return new ProductDto(product.getId(), product.getName(), product.getDescription(), product.getCategory(), product.getPrice().getValue(), product.getStock().getQuantity());
  }
}
