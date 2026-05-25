package com.example.qlbh.application.product.command;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UpdateProductCommand {

  @NotNull
  private Long id;

  private String name;

  private String description;  // optional — không @NotNull

  private String category;  // optional

  @DecimalMin("0.0")
  private BigDecimal price;

}
