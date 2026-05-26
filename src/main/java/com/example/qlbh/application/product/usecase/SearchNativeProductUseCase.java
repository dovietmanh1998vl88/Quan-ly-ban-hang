package com.example.qlbh.application.product.usecase;

import com.example.qlbh.application.product.dto.ProductDto;
import java.util.List;

public interface SearchNativeProductUseCase {

  List<ProductDto> execute(String keyword, int page, int size);
}
