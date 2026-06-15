package com.example.qlbh.application.order.usecase;

import com.example.qlbh.application.order.dto.OrderRevenueReportDto;

public interface RevenueReportUseCase {

  byte[] execute(OrderRevenueReportDto command);

}
