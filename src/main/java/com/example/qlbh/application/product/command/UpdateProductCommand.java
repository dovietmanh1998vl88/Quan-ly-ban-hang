package com.example.qlbh.application.product.command;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdateProductCommand {
  private String name;

  private String description;

  private String category;

  private BigDecimal price;
}
