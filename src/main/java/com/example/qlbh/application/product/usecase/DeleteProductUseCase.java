package com.example.qlbh.application.product.usecase;

import com.example.qlbh.application.product.command.DeleteProductCommand;

public interface DeleteProductUseCase {
  void execute(DeleteProductCommand command);
}
