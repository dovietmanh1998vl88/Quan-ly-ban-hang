package com.example.qlbh.infrastructure.persistence.product.excel;

import com.example.qlbh.domain.product.model.Product;
import java.io.IOException;
import java.util.List;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

@Component
public class ProductExcelExporter {

  public byte[] export(List<Product> products) {

    try (
        Workbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream out =
            new ByteArrayOutputStream()
    ) {

      Sheet sheet =
          workbook.createSheet("Products");

      CellStyle headerStyle = workbook.createCellStyle();

      // In đậm
      Font headerFont = workbook.createFont();
      headerFont.setBold(true);
      headerFont.setFontHeightInPoints((short) 12);
      headerStyle.setFont(headerFont);

      //END
      // Căn giữa theo chiều ngang
//      headerStyle.setAlignment(HorizontalAlignment.CENTER);

      // Căn giữa theo chiều dọc (tuỳ chọn)
      headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

      //fortmat pirce
      CellStyle currencyStyle = workbook.createCellStyle();
      DataFormat dataFormat = workbook.createDataFormat();
      currencyStyle.setDataFormat(dataFormat.getFormat("#,##0.00"));

      Row header =
          sheet.createRow(0);
      String[] columns = {"Id", "Name", "Price", "Stock"};
      for (int i = 0; i < columns.length; i++) {
        Cell cell = header.createCell(i);
        cell.setCellValue(columns[i]);
        cell.setCellStyle(headerStyle); // Áp dụng style đậm + căn giữa
      }

      for (int i = 0; i < products.size(); i++) {
        Row row = sheet.createRow(i + 1);
        Product p = products.get(i);
        row.createCell(0).setCellValue(p.getId());
        row.createCell(1).setCellValue(p.getName());
        // Cột Price — áp dụng currency style
        Cell priceCell = row.createCell(2);
        priceCell.setCellValue(p.getPrice().getValue().doubleValue());
        priceCell.setCellStyle(currencyStyle);
        row.createCell(3).setCellValue(p.getStock().getQuantity());
      }
      for (int i = 0; i < columns.length; i++) {
        sheet.autoSizeColumn(i);
        // Thêm padding ~15% vì autoSizeColumn đôi khi bị cắt sát
        int currentWidth = sheet.getColumnWidth(i);
        sheet.setColumnWidth(i, (int) (currentWidth * 1.15));
      }
      workbook.write(out);

      return out.toByteArray();

    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }
}
