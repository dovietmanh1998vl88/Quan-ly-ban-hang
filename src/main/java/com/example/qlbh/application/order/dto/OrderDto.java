package com.example.qlbh.application.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// application/order/dto/OrderDto.java
@Getter
@AllArgsConstructor
@Builder
public class OrderDto {

  private String id;
  private String customerId;
  private String status;
  private List<OrderItemDto> items;
  private BigDecimal totalAmount;
  private String createdAt;
}