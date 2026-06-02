package com.example.qlbh.infrastructure.persistence.oder.repository;

import com.example.qlbh.domain.order.model.Order;
import com.example.qlbh.domain.order.repository.OrderDomainRepository;
import com.example.qlbh.infrastructure.persistence.oder.entity.OrderEntity;
import com.example.qlbh.infrastructure.persistence.oder.mapper.OrderPersistenceMapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

// infrastructure/persistence/order/repository/OrderRepositoryImpl.java
@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderDomainRepository {

  private final OrderJpaRepository jpaRepository;
  private final OrderPersistenceMapper mapper;

  @Override
  public Optional<Order> findById(String id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public Optional<Order> findByIdForUpdate(String id) {
    return jpaRepository.findByIdForUpdate(id).map(mapper::toDomain);
  }

  @Override
  public List<Order> findByCustomerId(String customerId) {
    return jpaRepository.findByCustomerId(customerId)
        .stream().map(mapper::toDomain).toList();
  }

  @Override
  public Order save(Order order) {
    OrderEntity entity = mapper.toEntity(order);
    return mapper.toDomain(jpaRepository.save(entity));
  }
}
