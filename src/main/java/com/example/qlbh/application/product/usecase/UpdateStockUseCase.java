package com.example.qlbh.application.product.usecase;

import com.example.qlbh.application.product.command.UpdateStockCommand;
import com.example.qlbh.application.product.dto.ProductDto;

public interface UpdateStockUseCase {
  ProductDto execute(UpdateStockCommand command);
}
