package com.example.qlbh.application.product.service;

import com.example.qlbh.application.product.command.CreateProductCommand;
import com.example.qlbh.application.product.command.UpdateStockCommand;
import com.example.qlbh.application.product.dto.ProductDto;
import com.example.qlbh.application.product.mapper.ProductApplicationMapper;
import com.example.qlbh.common.enums.StockAction;
import com.example.qlbh.common.exception.NotFoundException;
import com.example.qlbh.domain.auth.repository.UserDomainRepository;
import com.example.qlbh.domain.product.valueobject.Price;
import com.example.qlbh.domain.product.model.Product;
import com.example.qlbh.domain.product.repository.ProductDomainRepository;
import com.example.qlbh.domain.product.valueobject.Stock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Unit Test cho ProductApplicationService.
 *
 * @ExtendWith(MockitoExtension.class) — dùng Mockito, không load Spring context → test chạy nhanh hơn @SpringBootTest
 * rất nhiều
 * <p>
 * Nguyên tắc test: AAA — Arrange, Act, Assert
 * - Arrange: chuẩn bị data và mock behavior
 * - Act: gọi method cần test
 * - Assert: kiểm tra kết quả
 */
@ExtendWith(MockitoExtension.class)
class ProductApplicationServiceTest {

  // @Mock — tạo object giả, mình kiểm soát behavior
  @Mock
  private ProductDomainRepository productRepository;

  @Mock
  private UserDomainRepository userRepository;

  @Mock
  private ProductApplicationMapper mapper;

  // @InjectMocks — tạo object thật, inject các @Mock vào
  @InjectMocks
  private ProductApplicationService productService;

  // ===== Helper tạo test data =====

//  private User buildUser(String username) {
//    return new User(
//        username,
//        "password",
//        Role.ADMIN
//    );
//  }


  private Product buildProduct(String id, String name, int stock) {
    return new Product(
        id,
        name,
        "Mô tả",
        "Danh mục",
        new Price(new BigDecimal("100000")),
        new Stock(stock)
    );
  }

  private ProductDto buildProductDto(String id, String name, int stock) {
    return ProductDto.builder()
        .id(id)
        .name(name)
        .description("Mô tả")
        .category("Danh mục")
        .price(new BigDecimal("100000"))
        .stock(stock)
        .build();
  }

  // ===== Test CreateProduct =====

  @Nested
  @DisplayName("CreateProduct UseCase")
  class CreateProductTest {

    @Test
    @DisplayName("Tạo sản phẩm thành công — trả về ProductDto có id")
    void createProduct_success() {

      // Arrange — chuẩn bị
      CreateProductCommand command = new CreateProductCommand();
      command.setName("Áo thun");
      command.setPrice(new BigDecimal("100000"));
      command.setStock(50);

      Product domainProduct = buildProduct(null, "Áo thun", 50);
      Product savedProduct = buildProduct("018e5b2a-3f4c-7abc-8def-123456789abc", "Áo thun", 50);
      ProductDto expectedDto = buildProductDto("018e5b2a-3f4c-7abc-8def-123456789abc", "Áo thun", 50);
//      User domainUser = buildUser("manhdv");

//      given(userRepository.findByUsername("manhdv"))
//          .willReturn(Optional.of(domainUser));
      // Mock behavior — khi gọi mapper/repo thì trả về gì
      given(productRepository.existsByNameIgnoreCase("Áo thun")).willReturn(false);
      given(mapper.toDomain(command)).willReturn(domainProduct);
      given(productRepository.save(domainProduct)).willReturn(savedProduct);
      given(mapper.toDto(savedProduct)).willReturn(expectedDto);

      // Act — gọi method cần test
      ProductDto result = productService.execute(command);

      // Assert — kiểm tra kết quả
      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo("018e5b2a-3f4c-7abc-8def-123456789abc");
      assertThat(result.getName()).isEqualTo("Áo thun");
      assertThat(result.getStock()).isEqualTo(50);

      // Verify — đảm bảo các method được gọi đúng số lần
      verify(productRepository).save(domainProduct);
      verify(productRepository).existsByNameIgnoreCase(domainProduct.getName());
      verify(mapper).toDto(savedProduct);
    }
  }

  // ===== Test GetProduct =====

  @Nested
  @DisplayName("GetProduct UseCase")
  class GetProductTest {

    @Test
    @DisplayName("Lấy sản phẩm theo id thành công")
    void getProduct_success() {
      // Arrange
      String productId = "018e5b2a-3f4c-7abc-8def-123456789abc";
      Product product = buildProduct(productId, "Áo thun", 50);
      ProductDto expectedDto = buildProductDto(productId, "Áo thun", 50);

      given(productRepository.findById(productId))
          .willReturn(Optional.of(product));
      given(mapper.toDto(product)).willReturn(expectedDto);

      // Act
      ProductDto result = productService.execute(productId);

      // Assert
      assertThat(result.getId()).isEqualTo(productId);
      assertThat(result.getName()).isEqualTo("Áo thun");
    }

    @Test
    @DisplayName("Lấy sản phẩm không tồn tại — throw NotFoundException")
    void getProduct_notFound_throwNotFoundException() {
      // Arrange
      String productId = "018e5b2a-3f4c-7abc-8def-123456789abc";
      given(productRepository.findById(productId))
          .willReturn(Optional.empty()); // giả DB không có

      // Act & Assert — kiểm tra exception được throw
      assertThatThrownBy(() -> productService.execute(productId))
          .isInstanceOf(NotFoundException.class)
          .hasMessageContaining("018e5b2a-3f4c-7abc-8def-123456789abc");

      // Verify mapper KHÔNG được gọi khi product không tồn tại
      verify(mapper, never()).toDto(any());
    }
  }

  // ===== Test UpdateStock =====

  @Nested
  @DisplayName("UpdateStock UseCase")
  class UpdateStockTest {

    @Test
    @DisplayName("Giảm stock thành công khi tồn kho đủ")
    void decreaseStock_success() {
      // Arrange
      String productId = "018e5b2a-3f4c-7abc-8def-123456789abc";
      Product product = buildProduct(productId, "Áo thun", 10);
      Product savedProduct = buildProduct(productId, "Áo thun", 7);
      ProductDto expectedDto = buildProductDto(productId, "Áo thun", 7);

      UpdateStockCommand command = new UpdateStockCommand(
          productId, 3, StockAction.DECREASE
      );

      given(productRepository.findByIdForUpdate(productId))
          .willReturn(Optional.of(product));
      given(productRepository.save(any(Product.class)))
          .willReturn(savedProduct);
      given(mapper.toDto(savedProduct)).willReturn(expectedDto);

      // Act
      ProductDto result = productService.execute(command);

      // Assert
      assertThat(result.getStock()).isEqualTo(7);
      verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Giảm stock thất bại khi tồn kho không đủ — throw BusinessException")
    void decreaseStock_insufficientStock_throwException() {
      // Arrange — stock chỉ có 2, muốn giảm 5
      String productId = "018e5b2a-3f4c-7abc-8def-123456789abc";
      Product product = buildProduct(productId, "Áo thun", 2);

      UpdateStockCommand command = new UpdateStockCommand(
          productId, 5, StockAction.DECREASE
      );

      given(productRepository.findByIdForUpdate(productId))
          .willReturn(Optional.of(product));

      // Act & Assert
      // Exception do Stock.decrease() throw, không phải Service
      // → đây là bằng chứng business rule nằm đúng chỗ trong Domain
      assertThatThrownBy(() -> productService.execute(command))
          .isInstanceOf(com.example.qlbh.common.exception.BusinessException.class)
          .hasMessageContaining("Tồn kho không đủ");

      // Verify save KHÔNG được gọi khi exception xảy ra
      verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Tăng stock thành công")
    void increaseStock_success() {
      // Arrange
      String productId = "018e5b2a-3f4c-7abc-8def-123456789abc";
      Product product = buildProduct(productId, "Áo thun", 10);
      Product savedProduct = buildProduct(productId, "Áo thun", 15);
      ProductDto expectedDto = buildProductDto(productId, "Áo thun", 15);

      UpdateStockCommand command = new UpdateStockCommand(
          productId, 5, StockAction.INCREASE
      );

      given(productRepository.findByIdForUpdate(productId))
          .willReturn(Optional.of(product));
      given(productRepository.save(any(Product.class)))
          .willReturn(savedProduct);
      given(mapper.toDto(savedProduct)).willReturn(expectedDto);

      // Act
      ProductDto result = productService.execute(command);

      // Assert
      assertThat(result.getStock()).isEqualTo(15);
    }

    @Test
    @DisplayName("Update stock sản phẩm không tồn tại — throw NotFoundException")
    void updateStock_productNotFound_throwNotFoundException() {
      // Arrange
      UpdateStockCommand command = new UpdateStockCommand(
          "018e5b2a-3f4c-7abc-8def-123456789abc", 5, StockAction.DECREASE
      );

      given(productRepository.findByIdForUpdate("018e5b2a-3f4c-7abc-8def-123456789abc"))
          .willReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> productService.execute(command))
          .isInstanceOf(NotFoundException.class)
          .hasMessageContaining("018e5b2a-3f4c-7abc-8def-123456789abc");

      verify(productRepository, never()).save(any());
    }
  }
}