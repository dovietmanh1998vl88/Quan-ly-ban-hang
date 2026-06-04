package com.example.qlbh.application.product.usecase;

import org.springframework.web.multipart.MultipartFile;

public interface ImportProductsUseCase {

  void importExcel(MultipartFile file);
}