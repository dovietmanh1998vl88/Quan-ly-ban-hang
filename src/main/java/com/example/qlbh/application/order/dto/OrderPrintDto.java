package com.example.qlbh.application.order.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OrderPrintDto {

  private String orderCode;

  private String customerName;

  private String phone;

  private String address;

  private String qrUrl;

  private BigDecimal totalAmount;

  private List<OrderItemPrintDto> items;
}