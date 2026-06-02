package com.example.qlbh.application.order.command;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

// application/order/command/ConfirmOrderCommand.java
@Getter
@AllArgsConstructor
public class ConfirmOrderCommand {

  @NotBlank
  private String orderId;
}