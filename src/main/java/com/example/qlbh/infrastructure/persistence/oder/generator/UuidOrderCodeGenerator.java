package com.example.qlbh.infrastructure.persistence.oder.generator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import com.example.qlbh.domain.order.service.OrderCodeGenerator;
import com.example.qlbh.domain.order.valueobject.OrderCode;

public class UuidOrderCodeGenerator implements OrderCodeGenerator {

    @Override
    public OrderCode next() {
        String code = "ORD-"
                + LocalDate.now()
                        .format(DateTimeFormatter.BASIC_ISO_DATE)
                + "-"
                + UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase();

        return new OrderCode(code);
    }

}
