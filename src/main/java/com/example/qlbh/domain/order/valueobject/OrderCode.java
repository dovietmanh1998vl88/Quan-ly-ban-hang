package com.example.qlbh.domain.order.valueobject;

import java.util.Objects;

public final class OrderCode {

    private final String value;

    public OrderCode(String value) {

        System.out.println("orderCode=333==" + value);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Order code cannot be blank");
        }

        this.value = value;
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof OrderCode that))
            return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
