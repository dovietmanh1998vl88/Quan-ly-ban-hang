package com.example.qlbh.infrastructure.persistence.product.entity;

import com.example.qlbh.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "products")
public class ProductEntity extends BaseEntity {

  @Column(nullable = false, unique = true, length = 255)
  private String name;

  @Column(nullable = false, length = 100)
  private String category;

  @Column(nullable = false)
  private String description;

  @Column(nullable = false, precision = 15, scale = 2)
  private BigDecimal price;

  @Column(nullable = false)
  private Integer stock;
}
