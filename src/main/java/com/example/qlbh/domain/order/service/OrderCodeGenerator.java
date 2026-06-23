package com.example.qlbh.domain.order.service;

import com.example.qlbh.domain.order.valueobject.OrderCode;

public interface OrderCodeGenerator {

    OrderCode next();
}
