// src/test/java/com/example/qlbh/presentation/product/ProductControllerIntegrationTest.java
package com.example.qlbh.presentation.product;

import com.example.qlbh.infrastructure.persistence.product.entity.ProductEntity;
import com.example.qlbh.infrastructure.persistence.product.repository.ProductJpaRepository;
import com.example.qlbh.presentation.auth.request.LoginRequest;
import com.example.qlbh.presentation.auth.request.RegisterRequest;
import com.example.qlbh.presentation.product.request.CreateProductRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProductControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private ProductJpaRepository productJpaRepository;

  // JWT token dùng chung cho các test cần auth
  private String accessToken;

  /**
   * @BeforeEach — chạy trước MỖI test case. Register + Login để lấy token, dùng cho các request cần auth.
   */
  @BeforeEach
  void setUp() throws Exception {
    // Register
    mockMvc.perform(
        post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new RegisterRequest("admin", "password123")
            ))
    );

    // Login — lấy token
    MvcResult loginResult = mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new LoginRequest("admin", "password123")
                ))
        )
        .andReturn();

    // Parse token từ response
    String responseBody = loginResult.getResponse().getContentAsString();
    accessToken = objectMapper.readTree(responseBody)
        .path("data")
        .path("accessToken")
        .asText();
  }

  // Helper tạo product trong DB cho test
  private ProductEntity createProductInDb(String name, int stock) {
    ProductEntity entity = new ProductEntity();
    entity.setName(name);
    entity.setCategory("Danh mục test");
    entity.setDescription("Mô tả test");
    entity.setPrice(new BigDecimal("100000"));
    entity.setStock(stock);
    return productJpaRepository.save(entity);
  }

  // ===== Create Product =====

  @Nested
  @DisplayName("POST /products")
  class CreateProductTest {

    @Test
    @DisplayName("Tạo sản phẩm thành công — có auth token")
    void createProduct_success() throws Exception {
      CreateProductRequest request = new CreateProductRequest();
      // Dùng reflection hoặc setter — Request dùng @Getter nên cần Lombok
      // Tạo qua JSON string trực tiếp cho đơn giản
      String requestJson = """
          {
              "name": "Áo thun test",
              "description": "Mô tả",
              "category": "Thời trang",
              "price": 150000,
              "stock": 100
          }
          """;

      mockMvc.perform(
              post("/products")
                  .contentType(MediaType.APPLICATION_JSON)
                  .header("Authorization", "Bearer " + accessToken)
                  .content(requestJson)
          )
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.name").value("Áo thun test"))
          .andExpect(jsonPath("$.data.stock").value(100))
          .andExpect(jsonPath("$.data.id").isNumber());
    }

    /**
     * @WithMockUser — mock user có role ADMIN Không cần Keycloak thật khi test
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN tạo sản phẩm thành công")
    void createProduct_adminRole_success() throws Exception {
      String requestJson = """
          {
              "name": "Áo thun test",
              "description": "Mô tả",
              "category": "Thời trang",
              "price": 150000,
              "stock": 100
          }
          """;

      mockMvc.perform(
              post("/products")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(requestJson)
          )
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.name").value("Áo thun test"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("CUSTOMER không được tạo sản phẩm — 403")
    void createProduct_customerRole_forbidden() throws Exception {
      String requestJson = """
          {
              "name": "Áo thun test",
              "price": 150000,
              "stock": 100
          }
          """;

      mockMvc.perform(
              post("/products")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(requestJson)
          )
          .andExpect(status().isForbidden());
    }


    @Test
    @DisplayName("Tạo sản phẩm không có token — trả 401")
    void createProduct_noToken_return401() throws Exception {
      String requestJson = """
          {
              "name": "Áo thun",
              "price": 150000,
              "stock": 100
          }
          """;

      mockMvc.perform(
              post("/products")
                  .contentType(MediaType.APPLICATION_JSON)
                  // Không có Authorization header
                  .content(requestJson)
          )
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Tạo sản phẩm thiếu name — trả 400")
    void createProduct_missingName_return400() throws Exception {
      String requestJson = """
          {
              "price": 150000,
              "stock": 100
          }
          """;

      mockMvc.perform(
              post("/products")
                  .contentType(MediaType.APPLICATION_JSON)
                  .header("Authorization", "Bearer " + accessToken)
                  .content(requestJson)
          )
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.success").value(false));
    }
  }

  // ===== Get Product =====

  @Nested
  @DisplayName("GET /products/{id}")
  class GetProductTest {

    @Test
    @DisplayName("Lấy sản phẩm theo id thành công")
    void getProduct_success() throws Exception {
      // Arrange — tạo product trong DB trước
      ProductEntity saved = createProductInDb("Quần jean", 30);

      mockMvc.perform(
              get("/products/" + saved.getId())
                  .header("Authorization", "Bearer " + accessToken)
          )
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.name").value("Quần jean"))
          .andExpect(jsonPath("$.data.stock").value(30));
    }

    @Test
    @DisplayName("Lấy sản phẩm không tồn tại — trả 404")
    void getProduct_notFound_return404() throws Exception {
      mockMvc.perform(
              get("/products/99999")
                  .header("Authorization", "Bearer " + accessToken)
          )
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.success").value(false));
    }
  }

  // ===== Update Stock =====

  @Nested
  @DisplayName("PUT /products/{id}/stock")
  class UpdateStockTest {

    @Test
    @DisplayName("Giảm stock thành công")
    void decreaseStock_success() throws Exception {
      // Arrange
      ProductEntity saved = createProductInDb("Giày sneaker", 20);

      String requestJson = """
          {
              "amount": 5,
              "action": "DECREASE"
          }
          """;

      mockMvc.perform(
              put("/products/" + saved.getId() + "/stock")
                  .contentType(MediaType.APPLICATION_JSON)
                  .header("Authorization", "Bearer " + accessToken)
                  .content(requestJson)
          )
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.stock").value(15));
    }

    @Test
    @DisplayName("Giảm stock quá số lượng — trả 400")
    void decreaseStock_insufficient_return400() throws Exception {
      // Arrange — stock chỉ có 3
      ProductEntity saved = createProductInDb("Túi xách", 3);

      String requestJson = """
          {
              "amount": 10,
              "action": "DECREASE"
          }
          """;

      mockMvc.perform(
              put("/products/" + saved.getId() + "/stock")
                  .contentType(MediaType.APPLICATION_JSON)
                  .header("Authorization", "Bearer " + accessToken)
                  .content(requestJson)
          )
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.success").value(false))
          .andExpect(jsonPath("$.message").value("Tồn kho không đủ"));
    }
  }

  // ===== Search Product =====

  @Nested
  @DisplayName("GET /products?keyword=")
  class SearchProductTest {

    @Test
    @DisplayName("Search theo keyword — trả đúng danh sách")
    void search_byKeyword_returnMatchedProducts() throws Exception {
      // Arrange — tạo 2 sản phẩm
      createProductInDb("Áo thun trắng", 10);
      createProductInDb("Áo thun đen", 5);
      createProductInDb("Quần jean", 20);

      mockMvc.perform(
              get("/products")
                  .param("keyword", "Áo thun")
                  .param("page", "0")
                  .param("size", "10")
                  .header("Authorization", "Bearer " + accessToken)
          )
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.items").isArray())
          .andExpect(jsonPath("$.data.items.length()").value(2))
          .andExpect(jsonPath("$.data.page").value(0))
          .andExpect(jsonPath("$.data.size").value(10));
    }

    @Test
    @DisplayName("Search không có kết quả — trả mảng rỗng")
    void search_noResult_returnEmpty() throws Exception {
      mockMvc.perform(
              get("/products")
                  .param("keyword", "xyznotexist")
                  .param("page", "0")
                  .param("size", "10")
                  .header("Authorization", "Bearer " + accessToken)
          )
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.items").isArray())
          .andExpect(jsonPath("$.data.items.length()").value(0));
    }
  }
}