package com.example.qlbh.application.product.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductDto{

  private String id;

  private String name;

  private String description;

  private String category;

  private BigDecimal price;

  private int stock;
}
