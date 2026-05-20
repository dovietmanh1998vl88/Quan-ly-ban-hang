package com.example.qlbh.application.product.usecase;

import com.example.qlbh.application.product.dto.ProductDto;

public interface SeachProductUseCase {
  ProductDto  execute(String keyword);
}
