package com.example.qlbh.presentation.product.request;

import com.example.qlbh.common.enums.StockAction;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public  record UpdateStockRequest(

    @NotNull
    @Min(1)
    Integer amount,

    @NotNull
    StockAction action  // client truyền "INCREASE" hoặc "DECREASE"
) {}
