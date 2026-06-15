package com.example.qlbh.application.order.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OrderRevenueReportDto {

  private LocalDate tungay;

  private LocalDate denngay;

  private int totalOder;

  private int totalCancelledDOder;

  private int totalFinishOder;

  private BigDecimal totalAmount;

  private String exportDate;

}
