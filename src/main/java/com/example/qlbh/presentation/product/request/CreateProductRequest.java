package com.example.qlbh.presentation.product.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;

@Getter
public class CreateProductRequest {

  @NotBlank
  private String name;

  private String description;  // optional — không @NotNull

  private String category;  // optional

  @NotNull
  @DecimalMin("0.0")
  private BigDecimal price;

  @Min(0)
  private int stock;
}
