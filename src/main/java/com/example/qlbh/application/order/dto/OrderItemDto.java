package com.example.qlbh.application.order.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// application/order/dto/OrderItemDto.java
@Getter
@AllArgsConstructor
@Builder
public class OrderItemDto {

  private String id;
  private String productId;
  private String productName;
  private int quantity;
  private BigDecimal unitPrice;
  private BigDecimal subtotal;
}