package com.example.qlbh.infrastructure.persistence.product.mapper;

import com.example.qlbh.domain.product.model.Price;
import com.example.qlbh.domain.product.model.Product;
import com.example.qlbh.domain.product.valueobject.Stock;
import com.example.qlbh.infrastructure.persistence.product.entity.ProductEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

  public ProductEntity toEntity(Product domain) {
    ProductEntity entity = new ProductEntity();
    entity.setId(domain.getId());
    entity.setName(domain.getName());
    entity.setDescription(domain.getDescription());
    entity.setCategory(domain.getCategory());
    entity.setPrice(domain.getPrice().getValue());
    entity.setStock(domain.getStock().getQuantity());
    return entity;
  }

  public Product toDomain(ProductEntity entity) {

    return new Product(entity.getId(), entity.getName(), entity.getDescription(), entity.getCategory(),new Price(entity.getPrice()) , new Stock(entity.getStock()) );
  }

}
