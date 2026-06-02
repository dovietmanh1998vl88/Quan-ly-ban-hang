package com.example.qlbh.infrastructure.persistence.oder.mapper;

import com.example.qlbh.domain.order.model.Order;
import com.example.qlbh.domain.order.model.OrderItem;
import com.example.qlbh.domain.order.valueobject.Money;
import com.example.qlbh.infrastructure.persistence.oder.entity.OrderEntity;
import com.example.qlbh.infrastructure.persistence.oder.entity.OrderItemEntity;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

// infrastructure/persistence/order/mapper/OrderPersistenceMapper.java
@Component
public class OrderPersistenceMapper {

  public Order toDomain(OrderEntity entity) {
    List<OrderItem> items = entity.getItems().stream()
        .map(this::toItemDomain)
        .toList();

    return new Order(
        entity.getId(),
        entity.getCustomerId(),
        entity.getStatus(),
        items,
        new Money(entity.getTotalAmount()),
        entity.getCreatedAt()
    );
  }

  private OrderItem toItemDomain(OrderItemEntity entity) {
    return new OrderItem(
        entity.getId(),
        entity.getProductId(),
        entity.getProductName(),
        entity.getQuantity(),
        new Money(entity.getUnitPrice())
    );
  }

  public OrderEntity toEntity(Order domain) {
    OrderEntity entity = new OrderEntity();
    entity.setId(domain.getId());
    entity.setCustomerId(domain.getCustomerId());
    entity.setStatus(domain.getStatus());
    entity.setTotalAmount(domain.getTotalAmount().getAmount());

    List<OrderItemEntity> itemEntities = domain.getItems().stream()
        .map(item -> toItemEntity(item, entity))
        .toList();

    entity.setItems(new ArrayList<>(itemEntities));
    return entity;
  }

  private OrderItemEntity toItemEntity(
      OrderItem domain,
      OrderEntity orderEntity
  ) {
    OrderItemEntity entity = new OrderItemEntity();
    entity.setId(domain.getId());
    entity.setOrder(orderEntity);
    entity.setProductId(domain.getProductId());
    entity.setProductName(domain.getProductName());
    entity.setQuantity(domain.getQuantity());
    entity.setUnitPrice(domain.getUnitPrice().getAmount());
    return entity;
  }
}
