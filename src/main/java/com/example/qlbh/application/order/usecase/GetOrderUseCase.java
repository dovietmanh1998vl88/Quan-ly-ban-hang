package com.example.qlbh.application.order.usecase;

import com.example.qlbh.application.order.dto.OrderDto;

public interface GetOrderUseCase {

  OrderDto execute(String orderId, String customerId);
}