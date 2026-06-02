package com.example.qlbh.domain.order.repository;

import com.example.qlbh.domain.order.model.Order;
import java.util.List;
import java.util.Optional;

// domain/order/repository/OrderDomainRepository.java
public interface OrderDomainRepository {

  Optional<Order> findById(String id);

  // Lock cho confirm/cancel để tránh race condition
  Optional<Order> findByIdForUpdate(String id);

  List<Order> findByCustomerId(String customerId);

  Order save(Order order);
}