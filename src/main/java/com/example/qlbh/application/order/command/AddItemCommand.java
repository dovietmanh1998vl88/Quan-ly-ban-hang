package com.example.qlbh.application.order.command;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

// application/order/command/AddItemCommand.java
@Getter
@Setter
public class AddItemCommand {

  @NotBlank
  private String orderId;

  @NotBlank
  private String productId;

  @Min(1)
  private int quantity;
}