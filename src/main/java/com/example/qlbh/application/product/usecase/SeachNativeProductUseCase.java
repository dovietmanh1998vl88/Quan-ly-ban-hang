package com.example.qlbh.application.product.usecase;

import com.example.qlbh.application.product.dto.ProductDto;

public interface SeachNativeProductUseCase {
  ProductDto execute(String keyword);
}
