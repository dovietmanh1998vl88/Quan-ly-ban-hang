package com.example.qlbh.application.order.usecase;

import com.example.qlbh.application.order.command.CancelOrderCommand;
import com.example.qlbh.application.order.dto.OrderDto;

public interface CancelOrderUseCase {

  OrderDto execute(CancelOrderCommand command);
}
