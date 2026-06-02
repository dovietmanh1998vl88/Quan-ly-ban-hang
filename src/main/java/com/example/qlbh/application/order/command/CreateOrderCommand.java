package com.example.qlbh.application.order.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateOrderCommand {

  // customerId lấy từ SecurityUtils — không do client truyền
  // Tránh client giả mạo customerId của người khác
  private String customerId;
}