package com.example.qlbh.infrastructure.persistence.oder.mapper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.example.qlbh.domain.order.model.Order;
import com.example.qlbh.domain.order.model.OrderItem;
import com.example.qlbh.domain.order.valueobject.Money;
import com.example.qlbh.domain.order.valueobject.OrderCode;
import com.example.qlbh.infrastructure.persistence.oder.entity.OrderEntity;
import com.example.qlbh.infrastructure.persistence.oder.entity.OrderItemEntity;

// infrastructure/persistence/order/mapper/OrderPersistenceMapper.java
@Component
public class OrderPersistenceMapper {

  public void updateEntity(OrderEntity entity, Order domain) {
    entity.setCustomerId(domain.getCustomerId());
    entity.setStatus(domain.getStatus());
    entity.setTotalAmount(domain.getTotalAmount().getAmount());

    // Cập nhật items
    List<OrderItemEntity> existingItems = entity.getItems();
    List<OrderItemEntity> updatedItems = new ArrayList<>();

    for (OrderItem item : domain.getItems()) {
      OrderItemEntity itemEntity = existingItems.stream()
          .filter(e -> e.getId().equals(item.getId()))
          .findFirst()
          .orElseGet(() -> toItemEntity(item, entity)); // tạo mới nếu không tồn tại

      itemEntity.setProductId(item.getProductId());
      itemEntity.setProductName(item.getProductName());
      itemEntity.setQuantity(item.getQuantity());
      itemEntity.setUnitPrice(item.getUnitPrice().getAmount());

      updatedItems.add(itemEntity);
    }

    // Xóa các items cũ không còn trong domain
    existingItems.removeIf(e -> updatedItems.stream().noneMatch(u -> u.getId().equals(e.getId())));
    // Thêm các items mới
    updatedItems.forEach(e -> {
      if (!existingItems.contains(e)) {
        existingItems.add(e);
      }
    });
  }

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
        entity.getCreatedAt(),
        new OrderCode(entity.getOderCode()));
  }

  private OrderItem toItemDomain(OrderItemEntity entity) {
    return new OrderItem(
        entity.getId(),
        entity.getProductId(),
        entity.getProductName(),
        entity.getQuantity(),
        new Money(entity.getUnitPrice()));
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
    entity.setOderCode(domain.getOrderCode().value());
    return entity;
  }

  private OrderItemEntity toItemEntity(
      OrderItem domain,
      OrderEntity orderEntity) {
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
