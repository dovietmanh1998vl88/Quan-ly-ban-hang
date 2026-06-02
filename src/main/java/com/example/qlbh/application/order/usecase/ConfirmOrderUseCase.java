package com.example.qlbh.application.order.usecase;

import com.example.qlbh.application.order.command.ConfirmOrderCommand;
import com.example.qlbh.application.order.dto.OrderDto;

public interface ConfirmOrderUseCase {

  OrderDto execute(ConfirmOrderCommand command);
}
