package com.example.qlbh.application.product.command;

import com.example.qlbh.common.enums.StockAction;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdateStockCommand {
  @NotNull
  private Long productId;

  @Min(1)
  private int amount;

  @NotNull
  private StockAction action;
}
