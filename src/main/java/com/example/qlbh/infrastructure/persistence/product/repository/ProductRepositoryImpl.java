package com.example.qlbh.infrastructure.persistence.product.repository;

import com.example.qlbh.domain.product.model.Product;
import com.example.qlbh.domain.product.repository.ProductDomainRepository;
import com.example.qlbh.infrastructure.persistence.product.entity.ProductEntity;
import com.example.qlbh.infrastructure.persistence.product.mapper.ProductMapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductDomainRepository {

  private final ProductJpaRepository jpaRepository;
  private final ProductMapper mapper;

  @Override
  public Optional<Product> findById(Long id) {
    return jpaRepository.findById(id)
        .map(mapper::toDomain);
  }

  @Override
  public List<Product> findAll() {
    return jpaRepository.findAll()
        .stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Override
  public Product save(Product product) {
    ProductEntity entity = mapper.toEntity(product);
    ProductEntity saved = jpaRepository.save(entity);
    // Quan trọng: trả về domain object từ entity đã save
    // vì entity sau save có id (nếu tạo mới) và updatedAt được điền
    return mapper.toDomain(saved);
  }

  @Override
  public void deleteById(Long id) {
    jpaRepository.deleteById(id);
  }
}
