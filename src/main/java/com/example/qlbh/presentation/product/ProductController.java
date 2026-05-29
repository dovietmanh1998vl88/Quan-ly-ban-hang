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
import com.example.qlbh.common.response.BaseResponse;
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
import org.springframework.security.access.prepost.PreAuthorize;
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
  @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
  public BaseResponse<PageResponse<ProductResponse>> search(
      @RequestParam String keyword,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size
  ) {
    PageResponse<ProductDto> result = seachProductUseCase.execute(keyword, page, size);
    List<ProductResponse> responseList = result.getItems()
        .stream()
        .map(mapper::toResponse)
        .toList();
    PageResponse<ProductResponse> response = new PageResponse<>(
        responseList,
        result.getPage(),
        result.getSize(),
        result.getTotal()
    );
    return BaseResponse.success(response);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
  public BaseResponse<ProductResponse> getProductById(@PathVariable String id) {
    ProductDto dto = getProductUseCase.execute(id);
    return BaseResponse.success(mapper.toResponse(dto));
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")  // chỉ ADMIN và STAFF mới tạo được sản phẩm
  public BaseResponse<ProductResponse> createProduct(
      @Valid @RequestBody CreateProductRequest request
  ) {
    ProductDto dto = createProductUseCase.execute(
        mapper.toCreateCommand(request)
    );
    return BaseResponse.success("Tạo sản phẩm thành côngxx", mapper.toResponse(dto));
  }


  @PutMapping("/{id}/stock")
  @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
  public BaseResponse<ProductResponse> updateStock(
      @PathVariable String id,
      @Valid @RequestBody UpdateStockRequest request
  ) {
    ProductDto dto = updateStockUseCase.execute(new UpdateStockCommand(
        id,
        request.amount(),
        request.action()
    ));
    return BaseResponse.success(mapper.toResponse(dto));
  }

  @PutMapping("/{id}/update-product")
  public BaseResponse<ProductResponse> updateProduct(
      @PathVariable String id,
      @Valid @RequestBody UpdateProductRequest request
  ) {
    ProductDto dto = updateProductUseCase.execute(new UpdateProductCommand(
        id,
        request.getName(),
        request.getDescription(),
        request.getCategory(),
        request.getPrice()
    ));
    return BaseResponse.success(mapper.toResponse(dto));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")  // chỉ ADMIN mới xóa được
  public BaseResponse<Void> deleteProduct(@PathVariable String id) {
    deleteProductUseCase.execute(new DeleteProductCommand(id));
    return BaseResponse.success("Xóa sản phẩm thành công", null);
  }
}
