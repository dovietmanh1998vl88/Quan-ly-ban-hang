package com.example.qlbh.application.order.command;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

// application/order/command/CancelOrderCommand.java
@Getter
@AllArgsConstructor
public class CancelOrderCommand {

  @NotBlank
  private String orderId;

  // customerId để verify ownership
  private String customerId;
}
