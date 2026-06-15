package com.example.qlbh.presentation.oder.mapper;

import com.example.qlbh.application.order.dto.OrderRevenueReportDto;
import com.example.qlbh.presentation.oder.request.OrderRevenueReportRequest;
import org.springframework.stereotype.Component;

@Component
public class OrderRevenuePresentationMapper {

  public OrderRevenueReportDto toCreateCommand(OrderRevenueReportRequest request) {
    return OrderRevenueReportDto.builder()
        .tungay(request.tungay())
        .denngay(request.denngay())
        .build();
  }
}
