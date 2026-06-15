package com.example.qlbh.application.product.port;

import com.example.qlbh.domain.product.model.Product;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface ProductExcelImporterPort {
  List<Product> read(MultipartFile file);
}
