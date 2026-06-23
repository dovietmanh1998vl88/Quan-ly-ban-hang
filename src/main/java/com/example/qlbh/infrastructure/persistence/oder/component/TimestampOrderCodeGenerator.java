package com.example.qlbh.infrastructure.persistence.oder.component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.example.qlbh.domain.order.service.OrderCodeGenerator;
import com.example.qlbh.domain.order.valueobject.OrderCode;

@Component
public class TimestampOrderCodeGenerator
        implements OrderCodeGenerator {

    @Override
    public OrderCode next() {

        String code = "ORD-" +
                LocalDate.now()
                        .format(DateTimeFormatter.BASIC_ISO_DATE)
                + "-" +
                UUID.randomUUID()
                        .toString()
                        .substring(0, 6)
                        .toUpperCase();

        return new OrderCode(code);
    }
}
