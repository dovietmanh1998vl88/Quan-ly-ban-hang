package com.example.qlbh.application.order.usecase;

import com.example.qlbh.application.order.command.AddItemCommand;
import com.example.qlbh.application.order.dto.OrderDto;

public interface AddItemToOrderUseCase {

  OrderDto execute(AddItemCommand command);
}
