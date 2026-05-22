package com.example.qlbh.application.product.service;

import com.example.qlbh.application.product.command.CreateProductCommand;
import com.example.qlbh.application.product.command.DeleteProductCommand;
import com.example.qlbh.application.product.command.UpdateProductCommand;
import com.example.qlbh.application.product.command.UpdateStockCommand;
import com.example.qlbh.application.product.dto.ProductDto;
import com.example.qlbh.application.product.mapper.ProductApplicationMapper;
import com.example.qlbh.application.product.usecase.CreateProductUseCase;
import com.example.qlbh.application.product.usecase.DeleteProductUseCase;
import com.example.qlbh.application.product.usecase.GetProductUseCase;
import com.example.qlbh.application.product.usecase.SeachProductUseCase;
import com.example.qlbh.application.product.usecase.UpdateProductUseCase;
import com.example.qlbh.application.product.usecase.UpdateStockUseCase;
import com.example.qlbh.common.enums.StockAction;
import com.example.qlbh.common.exception.NotFoundException;
import com.example.qlbh.common.response.ApiResponse;
import com.example.qlbh.common.response.PageResponse;
import com.example.qlbh.domain.product.model.Price;
import com.example.qlbh.domain.product.model.Product;
import com.example.qlbh.domain.product.repository.ProductDomainRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application Service — điều phối Use Case.
 *
 * Nhiệm vụ của Application Service:
 * 1. Nhận Command từ Presentation
 * 2. Gọi Domain objects/services để thực thi business logic
 * 3. Gọi Repository để persist
 * 4. Trả về DTO cho Presentation
 *
 * Application Service KHÔNG chứa business logic —
 * business logic nằm trong Domain (Product, Price, Stock).
 * Application Service chỉ "điều phối", giống nhạc trưởng.
 *
 * Implement nhiều UseCase interface thay vì một class lớn
 * → mỗi method rõ ràng thuộc về Use Case nào
 * → dễ tìm, dễ test từng Use Case độc lập
 */
@Service
@RequiredArgsConstructor
public class ProductApplicationService
    implements CreateProductUseCase,
    UpdateStockUseCase, UpdateProductUseCase ,
    GetProductUseCase, DeleteProductUseCase ,
    SeachProductUseCase {

  // Inject interface, không inject implementation cụ thể
  // → dễ thay đổi implementation sau này
  // → dễ mock khi viết unit test
  private final ProductDomainRepository productRepository;
  private final ProductApplicationMapper mapper;

  /**
   * Use Case: Tạo sản phẩm mới.
   *
   * Flow:
   * Command → Domain Object (validate trong constructor) → Save → DTO
   *
   * Nếu tên trống, giá âm, stock âm → Domain Object tự throw exception
   * → Application Service không cần validate thủ công
   */
  @Override
  @Transactional
  public ProductDto execute(CreateProductCommand command) {
    // mapper.toDomain() gọi constructor Product → validate ngay tại đây
    Product product = mapper.toDomain(command);
    Product saved = productRepository.save(product);
    return mapper.toDto(saved);
  }

  /**
   * Use Case: Lấy thông tin sản phẩm theo id.
   *
   * orElseThrow với NotFoundException → GlobalExceptionHandler bắt
   * → trả về 404 cho client, không để lộ NullPointerException
   */
  @Override
  @Transactional(readOnly = true)
  public ProductDto execute(Long id) {
    Product product = productRepository.findById(id)
        .orElseThrow(() ->
            new NotFoundException("Không tìm thấy sản phẩm id: " + id)
        );
    return mapper.toDto(product);
  }

  /**
   * Use Case: Cập nhật tồn kho.
   *
   * Chú ý: business logic (tồn kho không đủ, số lượng âm...)
   * nằm trong Stock.decrease() / Stock.increase() — không nằm ở đây.
   * Application Service chỉ load → gọi method domain → save.
   * Pattern này gọi là "Tell, Don't Ask" — ra lệnh cho domain làm,
   * không hỏi data rồi tự tính.
   */
  @Transactional
  @Override
  public ProductDto execute(UpdateStockCommand command) {
    Product product = productRepository
        .findByIdForUpdate(command.getProductId())
        .orElseThrow(() ->
            new NotFoundException(
                "Không tìm thấy sản phẩm id: " + command.getProductId()
            )
        );

    // Domain object tự xử lý business rule
    // Application chỉ quyết định gọi method nào
    if (command.getAction() == StockAction.INCREASE) {
      product.increaseStock(command.getAmount());
    } else {
      product.decreaseStock(command.getAmount());
    }

    Product saved = productRepository.save(product);
    return mapper.toDto(saved);
  }

  //update gia thong tin san pham, update gia, update category, update description, update name
  @Transactional
  @Override
  public ProductDto execute(UpdateProductCommand command) {
    Product product = productRepository
        .findById(command.getId())
        .orElseThrow(() ->
            new NotFoundException(
                "Không tìm thấy sản phẩm id: " + command.getId()
            )
        );
    product.updatePrice(new Price(command.getPrice()));
    product.updateProductInfo(command.getName(), command.getDescription(), command.getCategory());

    Product saved = productRepository.save(product);
    return mapper.toDto(saved);
  }

  @Override
  @Transactional
  public void execute(DeleteProductCommand command) {

    Product product = productRepository
        .findById(command.getProductId())
        .orElseThrow(() ->
            new NotFoundException(
                "Không tìm thấy sản phẩm id: " + command.getProductId()
            )
        );
    productRepository.deleteById(product);
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponse<ProductDto> execute(
      String keyword,
      int page,
      int size
  ) {
    int offset = page * size;

    List<Product> products =
        productRepository.findByNameContainingIgnoreCase(keyword, size, offset);

    List<ProductDto> dtos = products.stream()
        .map(mapper::toDto)
        .toList();

    return new PageResponse<>(dtos, page, size, dtos.size());
  }
}
