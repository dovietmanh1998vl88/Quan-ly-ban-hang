package com.example.qlbh.infrastructure.persistence.product.repository;

import com.example.qlbh.common.response.PageResponse;
import com.example.qlbh.infrastructure.persistence.product.entity.ProductEntity;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, Long> {

  Optional<ProductEntity> findById(Long id);

  List<ProductEntity> findAll();

  void deleteById(Long id);

  Page<ProductEntity> findByNameContainingIgnoreCase(
      String keyword,
      Pageable pageable
  );

  // infrastructure/.../ProductJpaRepository.java
  long countByNameContainingIgnoreCase(String keyword);
// JPA tự generate — không cần viết query

  /**
   * PESSIMISTIC_WRITE — lock row này lại. Các transaction khác muốn đọc/ghi row này phải chờ. Dùng cho các thao tác cần
   * tính chính xác cao: stock, balance...
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT p FROM ProductEntity p WHERE p.id = :id")
  Optional<ProductEntity> findByIdForUpdate(@Param("id") Long id);

  @Query(value = """
      SELECT
          p.id as id,
          p.name as name,
          p.price as price
      FROM product p
      WHERE MATCH(p.name)
      AGAINST(:keyword)
      """,
      nativeQuery = true)
  Page<ProductEntity> search(
      @Param("keyword") String keyword,
      Pageable pageable
  );
}
