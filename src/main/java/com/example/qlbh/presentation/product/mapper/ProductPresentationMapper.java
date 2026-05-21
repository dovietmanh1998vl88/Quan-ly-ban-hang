package com.example.qlbh.presentation.product.mapper;


import com.example.qlbh.application.product.command.CreateProductCommand;
import com.example.qlbh.application.product.dto.ProductDto;
import com.example.qlbh.presentation.product.request.CreateProductRequest;
import com.example.qlbh.presentation.product.response.ProductResponse;
import org.springframework.stereotype.Component;

@Component
public class ProductPresentationMapper {

  public CreateProductCommand toCommand(CreateProductRequest request) {
    CreateProductCommand command = new CreateProductCommand();
    command.setName(request.getName());
    command.setDescription(request.getDescription());
    command.setCategory(request.getCategory());
    command.setPrice(request.getPrice());
    command.setStock(request.getStock());
    return command;
  }

  public ProductResponse toResponse(ProductDto dto) {
    return new ProductResponse(
        dto.getId(),
        dto.getName(),
        dto.getDescription(),
        dto.getCategory(),
        dto.getPrice(),
        dto.getStock()
    );
  }
}
