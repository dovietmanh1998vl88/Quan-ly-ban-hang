package com.example.qlbh.infrastructure.persistence.product.excel;

import com.example.qlbh.domain.product.valueobject.Price;
import com.example.qlbh.domain.product.model.Product;
import com.example.qlbh.domain.product.valueobject.Stock;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ProductExcelImporter {

  public List<Product> read(
      MultipartFile file) {

    List<Product> products =
        new ArrayList<>();

    try (
        Workbook workbook =
            new XSSFWorkbook(
                file.getInputStream())
    ) {

      Sheet sheet =
          workbook.getSheetAt(0);

      for (int i = 1;
          i <= sheet.getLastRowNum();
          i++) {

        Row row =
            sheet.getRow(i);

        if (row == null) {
          continue;
        }

        String name =
            row.getCell(0)
                .getStringCellValue();

        String description =
            row.getCell(1)
                .getStringCellValue();

        String category =
            row.getCell(2)
                .getStringCellValue();

        BigDecimal price =
            BigDecimal.valueOf(
                row.getCell(3)
                    .getNumericCellValue());

        int stock =
            (int) row.getCell(4)
                .getNumericCellValue();

        Product product =
            new Product(
                name,
                description,
                category,
                new Price(price),
                new Stock(stock)
            );

        products.add(product);
      }

      return products;

    } catch (Exception ex) {

      throw new RuntimeException(
          "Import Excel lỗi",
          ex
      );
    }
  }
}
