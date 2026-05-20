package com.example.qlbh.application.product.command;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/**
 * Command — đối tượng chứa dữ liệu đầu vào cho Use Case.
 *
 * Command Pattern trong CA:
 * - Tách biệt "ý định" (CreateProduct) khỏi "cách thực hiện"
 * - Presentation layer tạo Command từ Request rồi truyền vào UseCase
 * - UseCase không biết gì về HTTP Request, chỉ nhận Command
 *
 * Lợi ích: UseCase có thể được gọi từ nhiều nguồn
 * (REST Controller, gRPC, Kafka consumer, Schedule job...)
 * mà không cần thay đổi.
 */
@Getter
@Setter
public class CreateProductCommand {

      @NotBlank
      private String name;

      private String description;  // optional — không @NotNull

      private String category;  // optional

      @NotNull
      @DecimalMin("0.0")
      private BigDecimal price;

      @Min(0)
      private int stock;
}
