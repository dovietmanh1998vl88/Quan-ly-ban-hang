package com.example.qlbh.application.product.usecase;

import com.example.qlbh.application.product.dto.ProductDto;
import com.example.qlbh.common.response.PageResponse;
import java.util.List;
import org.springframework.data.domain.Page;

public interface SeachProductUseCase {
  PageResponse<ProductDto> execute(String keyword, int page,int size);
}
