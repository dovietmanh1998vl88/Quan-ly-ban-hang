package com.example.qlbh.infrastructure.persistence.product.repository;

import com.example.qlbh.infrastructure.persistence.product.entity.ProductEntity;
import com.example.qlbh.infrastructure.persistence.user.entity.UserEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, Long> {

  Optional<ProductEntity> findById(Long id);

  List<ProductEntity> findAll();

  void deleteById(Long id);
}
