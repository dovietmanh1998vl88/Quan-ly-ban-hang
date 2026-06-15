package com.example.qlbh.application.product.port;

import com.example.qlbh.domain.product.model.Product;
import java.util.List;

public interface ProductExcelReaderPort {
  byte[] export(List<Product> products);
}
