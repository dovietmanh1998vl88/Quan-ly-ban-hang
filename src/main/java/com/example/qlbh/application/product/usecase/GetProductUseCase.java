package com.example.qlbh.application.product.usecase;

import com.example.qlbh.application.product.dto.ProductDto;

public interface GetProductUseCase {
  ProductDto execute(Long productId);
}
