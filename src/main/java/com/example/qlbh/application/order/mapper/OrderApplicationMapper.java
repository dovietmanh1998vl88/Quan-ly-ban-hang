package com.example.qlbh.application.order.mapper;

import com.example.qlbh.application.order.dto.OrderDto;
import com.example.qlbh.application.order.dto.OrderItemDto;
import com.example.qlbh.domain.order.model.Order;
import com.example.qlbh.domain.order.model.OrderItem;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Component;

// application/order/mapper/OrderApplicationMapper.java
@Component
public class OrderApplicationMapper {

  public OrderDto toDto(Order order) {
    List<OrderItemDto> itemDtos = order.getItems().stream()
        .map(this::toItemDto)
        .toList();

    return OrderDto.builder()
        .id(order.getId())
        .customerId(order.getCustomerId())
        .status(order.getStatus().name())
        .items(itemDtos)
        .totalAmount(order.getTotalAmount().getAmount())

        .createdAt(order.getCreatedAt() != null ? order.getCreatedAt().atZone(ZoneId.of("Asia/Ho_Chi_Minh"))
            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) : null)
        .build();
  }

  private OrderItemDto toItemDto(OrderItem item) {
    return OrderItemDto.builder()
        .id(item.getId())
        .productId(item.getProductId())
        .productName(item.getProductName())
        .quantity(item.getQuantity())
        .unitPrice(item.getUnitPrice().getAmount())
        .subtotal(item.subtotal().getAmount())
        .build();
  }
}
