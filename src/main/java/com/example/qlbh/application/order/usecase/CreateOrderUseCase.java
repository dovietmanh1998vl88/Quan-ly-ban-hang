package com.example.qlbh.application.order.usecase;

import com.example.qlbh.application.order.command.CreateOrderCommand;
import com.example.qlbh.application.order.dto.OrderDto;

public interface CreateOrderUseCase {

  OrderDto execute(CreateOrderCommand command);
}
