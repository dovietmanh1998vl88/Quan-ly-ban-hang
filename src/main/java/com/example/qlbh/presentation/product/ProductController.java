package com.example.qlbh.presentation.product;

import com.example.qlbh.application.product.command.DeleteProductCommand;
import com.example.qlbh.application.product.command.UpdateProductCommand;
import com.example.qlbh.application.product.command.UpdateStockCommand;
import com.example.qlbh.application.product.dto.ProductDto;
import com.example.qlbh.application.product.usecase.CreateProductUseCase;
import com.example.qlbh.application.product.usecase.DeleteProductUseCase;
import com.example.qlbh.application.product.usecase.GetProductUseCase;
import com.example.qlbh.application.product.usecase.SearchProductUseCase;
import com.example.qlbh.application.product.usecase.UpdateProductUseCase;
import com.example.qlbh.application.product.usecase.UpdateStockUseCase;
import com.example.qlbh.common.response.ApiResponse;
import com.example.qlbh.common.response.PageResponse;
import com.example.qlbh.presentation.product.mapper.ProductPresentationMapper;
import com.example.qlbh.presentation.product.request.CreateProductRequest;
import com.example.qlbh.presentation.product.request.UpdateProductRequest;
import com.example.qlbh.presentation.product.request.UpdateStockRequest;
import com.example.qlbh.presentation.product.response.ProductResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

  private final UpdateStockUseCase updateStockUseCase;
  private final ProductPresentationMapper mapper;
  private final CreateProductUseCase createProductUseCase;
  private final UpdateProductUseCase updateProductUseCase;
  private final GetProductUseCase getProductUseCase;
  private final DeleteProductUseCase deleteProductUseCase;
  private final SearchProductUseCase seachProductUseCase;
  private static final Logger log = LoggerFactory.getLogger(ProductController.class);

  @GetMapping
  public ApiResponse<PageResponse<ProductResponse>> search(
      @RequestParam String keyword,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size
  ) {
    PageResponse<ProductDto> result  = seachProductUseCase.execute(keyword, page, size);
    List<ProductResponse> responseList = result .getItems()
        .stream()
        .map(mapper::toResponse)
        .toList();
    PageResponse<ProductResponse> response = new PageResponse<>(
        responseList,
        result.getPage(),
        result.getSize(),
        result.getTotal()
    );
    return ApiResponse.success(response);
  }

  @GetMapping("/{id}")
  public ApiResponse<ProductResponse> getProductById(@PathVariable Long id) {
    ProductDto dto = getProductUseCase.execute(id);
    return ApiResponse.success(mapper.toResponse(dto));}

  @PostMapping
  public ApiResponse<ProductResponse> createProduct(
      @Valid @RequestBody CreateProductRequest request
  ) {
    ProductDto dto = createProductUseCase.execute(
        mapper.toCreateCommand(request)
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

  @PutMapping("/{id}/update-product")
  public ApiResponse<ProductResponse> updateProduct(
      @PathVariable Long id,
      @Valid @RequestBody UpdateProductRequest request
  ) {
    ProductDto dto = updateProductUseCase.execute(new UpdateProductCommand(
        id,
        request.getName(),
        request.getDescription(),
        request.getCategory(),
        request.getPrice()
    ));
    return ApiResponse.success(mapper.toResponse(dto));
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> deleteProduct(@PathVariable Long id) {
    deleteProductUseCase.execute(new DeleteProductCommand(id));
    return ApiResponse.success("Xóa sản phẩm thành công", null);
  }
}
