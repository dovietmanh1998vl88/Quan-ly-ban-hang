package com.example.qlbh.infrastructure.persistence.export.pdf;

import com.example.qlbh.application.order.dto.OrderPrintDto;
import com.example.qlbh.application.order.dto.OrderRevenueReportDto;
import com.example.qlbh.application.order.port.PdfGenerator;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.io.ByteArrayOutputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
@RequiredArgsConstructor
public class OpenHtmlPdfGenerator implements PdfGenerator {

  private final TemplateEngine templateEngine;

  @Override
  public byte[] generateOrderPdf(OrderPrintDto order) {

    Context context = new Context();

    context.setVariable("order", order);

    String html =
        templateEngine.process(
            "pdf/order-print",
            context);

    ByteArrayOutputStream outputStream =
        new ByteArrayOutputStream();

    PdfRendererBuilder builder =
        new PdfRendererBuilder();

    builder.useFastMode();

    builder.withHtmlContent(
        html,
        null);

    builder.toStream(outputStream);

    try {
      builder.useFont(
          () -> getClass()
              .getResourceAsStream(
                  "/fonts/Roboto-Regular.ttf"),
          "Roboto");
    } catch (Exception exception) {
      System.out.println(
          "Cannot load font: " + exception.getMessage());
    }
    System.out.println(
        getClass().getResource("/fonts/Roboto-Regular.ttf")
    );
    try {
      builder.run();
    } catch (Exception e) {
      throw new RuntimeException(
          "Cannot generate PDF",
          e);
    }

    return outputStream.toByteArray();
  }

  @Override
  public byte[] PdfOrderRevenueGenerator(OrderRevenueReportDto order) {
    Context context = new Context();

    context.setVariable("orderRevenue", order);

    String html =
        templateEngine.process(
            "pdf/orderRevenue-print",
            context);

    ByteArrayOutputStream outputStream =
        new ByteArrayOutputStream();

    PdfRendererBuilder builder =
        new PdfRendererBuilder();

    builder.useFastMode();

    builder.withHtmlContent(
        html,
        null);

    builder.toStream(outputStream);

    try {
      builder.useFont(
          () -> getClass()
              .getResourceAsStream(
                  "/fonts/Roboto-Regular.ttf"),
          "Roboto");
    } catch (Exception exception) {
      System.out.println(
          "Cannot load font: " + exception.getMessage());
    }
    try {
      builder.run();
    } catch (Exception e) {
      throw new RuntimeException(
          "Cannot generate PDF",
          e);
    }

    return outputStream.toByteArray();
  }
}
