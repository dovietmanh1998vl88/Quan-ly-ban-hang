package com.example.qlbh.presentation.oder.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

// presentation/order/request/AddItemRequest.java
public record AddItemRequest(
    @NotBlank String productId,
    @Min(1) int quantity
) {

}
