package com.example.qlbh.infrastructure.persistence.product.excel;

import com.example.qlbh.common.exception.BusinessException;
import com.example.qlbh.domain.product.model.Product;
import com.example.qlbh.domain.product.valueobject.Price;
import com.example.qlbh.domain.product.valueobject.Stock;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
public class ProductExcelImporter {

  // Giữ method cũ để backward compatible
  public List<Product> read(MultipartFile file) {
    try {
      return readFromStream(file.getInputStream());
    } catch (Exception e) {
      throw new BusinessException("Đọc file Excel thất bại: " + e.getMessage());
    }
  }

  // Method mới — nhận InputStream (dùng khi download từ MinIO)
  public List<Product> readFromStream(InputStream inputStream) {
    List<Product> products = new ArrayList<>();
    List<String> errors = new ArrayList<>();

    try (Workbook workbook = new XSSFWorkbook(inputStream)) {
      Sheet sheet = workbook.getSheetAt(0);
      int lastRow = sheet.getLastRowNum();

      log.info("[Excel] Reading {} data rows (excluding header)", lastRow);

      for (int i = 1; i <= lastRow; i++) {
        Row row = sheet.getRow(i);
        if (row == null || isRowEmpty(row)) {
          continue;
        }

        try {
          Product product = parseRow(row, i);
          products.add(product);
        } catch (Exception e) {
          errors.add("Dòng " + (i + 1) + ": " + e.getMessage());
          log.warn("[Excel] Row {} error: {}", i + 1, e.getMessage());
        }
      }

      if (!errors.isEmpty()) {
        // Nếu có lỗi → throw với toàn bộ danh sách lỗi
        throw new BusinessException(
            "File Excel có " + errors.size() + " lỗi:\n" + String.join("\n", errors)
        );
      }

      return products;

    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      throw new BusinessException("File Excel không hợp lệ: " + e.getMessage());
    }
  }

  // ===== Private helpers =====

  private Product parseRow(Row row, int rowIndex) {
    // Column order: name | description | category | price | stock
    String name = getStringCell(row, 0, "name", rowIndex);
    String description = getStringCellOptional(row, 1);
    String category = getStringCell(row, 2, "category", rowIndex);
    BigDecimal price = getNumericCell(row, 3, "price", rowIndex);
    int stock = getIntCell(row, 4, "stock", rowIndex);

    return new Product(name, description, category, new Price(price), new Stock(stock));
  }

  private String getStringCell(Row row, int col, String fieldName, int rowIndex) {
    Cell cell = row.getCell(col);
    if (cell == null || cell.getCellType() == CellType.BLANK) {
      throw new BusinessException("Thiếu trường '" + fieldName + "'");
    }
    String value = cell.getStringCellValue().trim();
    if (value.isBlank()) {
      throw new BusinessException("Trường '" + fieldName + "' không được trống");
    }
    return value;
  }

  private String getStringCellOptional(Row row, int col) {
    Cell cell = row.getCell(col);
    if (cell == null || cell.getCellType() == CellType.BLANK) {
      return "";
    }
    return cell.getStringCellValue().trim();
  }

  private BigDecimal getNumericCell(Row row, int col, String fieldName, int rowIndex) {
    Cell cell = row.getCell(col);
    if (cell == null || cell.getCellType() != CellType.NUMERIC) {
      throw new BusinessException("Trường '" + fieldName + "' phải là số");
    }
    double val = cell.getNumericCellValue();
    if (val < 0) {
      throw new BusinessException("Trường '" + fieldName + "' không được âm");
    }
    return BigDecimal.valueOf(val);
  }

  private int getIntCell(Row row, int col, String fieldName, int rowIndex) {
    return getNumericCell(row, col, fieldName, rowIndex).intValue();
  }

  private boolean isRowEmpty(Row row) {
    for (int i = 0; i < 5; i++) {
      Cell cell = row.getCell(i);
      if (cell != null && cell.getCellType() != CellType.BLANK) {
        return false;
      }
    }
    return true;
  }
}