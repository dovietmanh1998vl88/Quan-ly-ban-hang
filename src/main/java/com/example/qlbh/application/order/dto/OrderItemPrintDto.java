package com.example.qlbh.application.order.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OrderItemPrintDto {

  private String productName;

  private Integer quantity;

  private BigDecimal price;
}