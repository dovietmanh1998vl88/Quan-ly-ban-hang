package com.example.qlbh.infrastructure.persistence.export.pdf;

import com.example.qlbh.application.order.dto.OrderPrintDto;

public interface PdfGenerator {

  byte[] generateOrderPdf(OrderPrintDto order);

}
