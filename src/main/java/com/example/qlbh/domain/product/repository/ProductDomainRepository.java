package com.example.qlbh.domain.product.repository;

import com.example.qlbh.domain.product.model.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

/**
 * Repository interface của Domain layer.
 *
 * Trong DDD, Repository là "collection-like interface" —
 * bên ngoài nhìn vào như đang làm việc với một collection in-memory,
 * không biết phía sau là MySQL, MongoDB hay bất kỳ gì.
 *
 * Quan trọng:
 * - Interface nằm ở DOMAIN — domain định nghĩa "tôi cần gì"
 * - Implementation nằm ở INFRASTRUCTURE — infra quyết định "làm thế nào"
 * - Đây là Dependency Inversion Principle (DIP) trong SOLID
 *
 * Chỉ có Repository cho Aggregate Root (Product),
 * không có Repository riêng cho Price hay Stock
 * vì chúng là Value Object, không tồn tại độc lập.
 */
public interface ProductDomainRepository {

  // Trả về Optional — buộc caller phải xử lý trường hợp không tìm thấy
  // Thay vì trả null gây NullPointerException ngầm
  Optional<Product> findById(Long id);

  Optional<Product> findByIdForUpdate(Long id);

  List<Product> findByNameContainingIgnoreCase(String keyword, int limit, int offset);


  List<Product> findAll();

  // Nhận và trả về Domain object (Product), không phải Entity (ProductEntity)
  // → Application layer không biết sự tồn tại của ProductEntity
  Product save(Product product);

  void deleteById(Product product);
}
