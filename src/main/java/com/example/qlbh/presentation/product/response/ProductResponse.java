package com.example.qlbh.presentation.product.response;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductResponse {

  private String id;

  private String name;

  private String description;

  private String category;

  private BigDecimal price;

  private int stock;
}
