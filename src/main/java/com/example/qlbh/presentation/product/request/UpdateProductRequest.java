package com.example.qlbh.presentation.product.request;

import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import lombok.Getter;

@Getter
public class UpdateProductRequest {

  private String name;

  private String description;  // optional — không @NotNull

  private String category;  // optional

  @DecimalMin("0.0")
  private BigDecimal price;
}
