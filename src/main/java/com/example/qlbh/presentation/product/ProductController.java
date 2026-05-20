package com.example.qlbh.presentation.product;

import com.example.qlbh.application.product.command.UpdateStockCommand;
import com.example.qlbh.application.product.dto.ProductDto;
import com.example.qlbh.application.product.usecase.CreateProductUseCase;
import com.example.qlbh.application.product.usecase.UpdateStockUseCase;
import com.example.qlbh.common.response.ApiResponse;
import com.example.qlbh.presentation.product.mapper.ProductMapper;
import com.example.qlbh.presentation.product.request.CreateProductRequest;
import com.example.qlbh.presentation.product.request.UpdateStockRequest;
import com.example.qlbh.presentation.product.response.ProductResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

  private final UpdateStockUseCase updateStockUseCase;
  private final ProductMapper mapper;
  private final CreateProductUseCase createProductUseCase;

  @PostMapping
  public ApiResponse<ProductResponse> create(
      @Valid @RequestBody CreateProductRequest request
  ) {
    ProductDto dto = createProductUseCase.execute(
        mapper.toCommand(request)
    );
    return ApiResponse.success("Tạo sản phẩm thành công", mapper.toResponse(dto));
  }


  @PutMapping("/{id}/stock")
  public ApiResponse<ProductResponse> updateStock(
      @PathVariable Long id,
      @Valid @RequestBody UpdateStockRequest request
  ) {
    ProductDto dto = updateStockUseCase.execute(new UpdateStockCommand(
        id,
        request.amount(),
        request.action()
    ));
    return ApiResponse.success(mapper.toResponse(dto));
  }
}
