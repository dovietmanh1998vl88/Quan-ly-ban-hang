package com.example.qlbh.application.product.usecase;

import com.example.qlbh.application.product.command.CreateProductCommand;
import com.example.qlbh.application.product.dto.ProductDto;

public interface CreateProductUseCase {
  ProductDto execute(CreateProductCommand command);
}
