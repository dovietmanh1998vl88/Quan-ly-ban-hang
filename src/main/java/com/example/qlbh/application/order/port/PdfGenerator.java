package com.example.qlbh.application.order.port;

import com.example.qlbh.application.order.dto.OrderPrintDto;
import com.example.qlbh.application.order.dto.OrderRevenueReportDto;

public interface PdfGenerator {

  byte[] generateOrderPdf(OrderPrintDto order);

  byte[] PdfOrderRevenueGenerator(OrderRevenueReportDto orderRevenueReportDto);

}
