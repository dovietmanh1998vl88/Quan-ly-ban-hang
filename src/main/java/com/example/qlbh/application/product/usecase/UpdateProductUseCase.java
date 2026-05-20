package com.example.qlbh.application.product.usecase;

import com.example.qlbh.application.product.command.UpdateProductCommand;
import com.example.qlbh.application.product.dto.ProductDto;

public interface UpdateProductUseCase {
  ProductDto execute(UpdateProductCommand command);
}
