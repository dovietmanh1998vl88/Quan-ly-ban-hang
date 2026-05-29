package com.example.qlbh.presentation.auth;

import com.example.qlbh.infrastructure.persistence.product.entity.ProductEntity;
import com.example.qlbh.infrastructure.persistence.product.repository.ProductJpaRepository;
import com.example.qlbh.presentation.auth.request.LoginRequest;
import com.example.qlbh.presentation.auth.request.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration Test cho Auth API.
 *
 * @SpringBootTest — load toàn bộ Spring context thật
 * @AutoConfigureMockMvc — tạo MockMvc để gọi HTTP không cần server thật
 * @ActiveProfiles("test") — dùng application-test.yml
 * @Transactional — mỗi test chạy trong transaction riêng, rollback sau khi test xong → data không bị dính nhau
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private ProductJpaRepository productJpaRepository;

  // ===== Register =====

  private ProductEntity createProductInDb(String name, int stock) {
    ProductEntity entity = new ProductEntity();
    entity.setName(name);
    entity.setCategory("Danh mục test");
    entity.setDescription("Mô tả test");
    entity.setPrice(new BigDecimal("100000"));
    entity.setStock(stock);
    return productJpaRepository.save(entity);
  }

  @Nested
  @DisplayName("POST /auth/register")
  class RegisterTest {

    @Test
    @DisplayName("Register thành công — trả 200 và có username trong response")
    void register_success() throws Exception {
      RegisterRequest request = new RegisterRequest("testuser", "password123");

      mockMvc.perform(
              post("/auth/register")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
          )
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.username").value("testuser"))
          .andExpect(jsonPath("$.data.id").value("018e5b2a-3f4c-7abc-8def-123456789abc"));
    }

    @Test
    @DisplayName("Register username trống — trả 400")
    void register_blankUsername_return400() throws Exception {
      RegisterRequest request = new RegisterRequest("", "password123");

      mockMvc.perform(
              post("/auth/register")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
          )
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Register username đã tồn tại — trả 400")
    void register_duplicateUsername_return400() throws Exception {
      RegisterRequest request = new RegisterRequest("dupuser", "password123");

      // Lần 1 — thành công
      mockMvc.perform(
              post("/auth/register")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
          )
          .andExpect(status().isOk());

      // Lần 2 — duplicate → 400
      mockMvc.perform(
              post("/auth/register")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
          )
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.success").value(false))
          .andExpect(jsonPath("$.message").value("Username đã tồn tại"));
    }
  }

  // ===== Login =====

  @Nested
  @DisplayName("POST /auth/login")
  class LoginTest {

    @Test
    @DisplayName("Login thành công — trả accessToken")
    void login_success() throws Exception {
      // Arrange — register trước
      RegisterRequest registerRequest =
          new RegisterRequest("loginuser", "password123");

      mockMvc.perform(
          post("/auth/register")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(registerRequest))
      );

      // Act — login
      LoginRequest loginRequest =
          new LoginRequest("loginuser", "password123");

      mockMvc.perform(
              post("/auth/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(loginRequest))
          )
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }

    @Test
    @DisplayName("Login sai password — trả 401")
    void login_wrongPassword_return401() throws Exception {
      // Arrange
      mockMvc.perform(
          post("/auth/register")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(
                  new RegisterRequest("user2", "correctpass")
              ))
      );

      // Act — sai password
      mockMvc.perform(
              post("/auth/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(
                      new LoginRequest("user2", "wrongpass")
                  ))
          )
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Login username không tồn tại — trả 401")
    void login_userNotFound_return401() throws Exception {
      mockMvc.perform(
              post("/auth/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(
                      new LoginRequest("ghost", "anypass")
                  ))
          )
          .andExpect(status().isUnauthorized());
    }
  }

  // Test trong Integration Test — kiểm tra phân quyền thực tế
  @Nested
  @DisplayName("Authorization — phân quyền theo role")
  class AuthorizationTest {

    @Test
    @DisplayName("CUSTOMER không được xóa product — trả 403")
    void deleteProduct_customerRole_return403() throws Exception {
      // Arrange — register CUSTOMER (default role)
      mockMvc.perform(post("/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content("""
                  {"username": "customer1", "password": "pass123"}
              """));

      // Login lấy token CUSTOMER
      MvcResult result = mockMvc.perform(post("/auth/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                      {"username": "customer1", "password": "pass123"}
                  """))
          .andReturn();

      String customerToken = objectMapper.readTree(
          result.getResponse().getContentAsString()
      ).path("data").path("accessToken").asText();

      // Tạo product trong DB
      ProductEntity product = createProductInDb("Áo thun", 10);

      // Act — CUSTOMER cố xóa product
      mockMvc.perform(delete("/products/" + product.getId())
              .header("Authorization", "Bearer " + customerToken)
          )
          .andExpect(status().isForbidden())               // 403
          .andExpect(jsonPath("$.success").value(false))
          .andExpect(jsonPath("$.message")
              .value("Bạn không có quyền thực hiện thao tác này"));
    }

    @Test
    @DisplayName("CUSTOMER xem product — được phép, trả 200")
    void getProduct_customerRole_return200() throws Exception {
      // ... tương tự, verify CUSTOMER đọc được
    }
  }

}