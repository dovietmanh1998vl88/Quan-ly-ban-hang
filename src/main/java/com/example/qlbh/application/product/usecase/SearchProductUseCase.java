package com.example.qlbh.application.product.usecase;

import com.example.qlbh.application.product.dto.ProductDto;
import com.example.qlbh.common.response.PageResponse;

public interface SearchProductUseCase {
  PageResponse<ProductDto> execute(String keyword, int page,int size);
}
