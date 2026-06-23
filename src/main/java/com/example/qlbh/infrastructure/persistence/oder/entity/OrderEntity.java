package com.example.qlbh.infrastructure.persistence.oder.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.example.qlbh.common.enums.OrderStatus;
import com.example.qlbh.infrastructure.persistence.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

// infrastructure/persistence/order/entity/OrderEntity.java
@Getter
@Setter
@Entity
@Table(name = "orders")
public class OrderEntity extends BaseEntity {

  @Column(nullable = false)
  private String customerId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private OrderStatus status;

  @Column(nullable = false, precision = 15, scale = 2)
  private BigDecimal totalAmount;

  @Column(name = "order_code")
  private String oderCode;

  // OneToMany — load cùng Order
  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER // luôn load
                                                                                                          // items cùng
                                                                                                          // order
  )
  private List<OrderItemEntity> items = new ArrayList<>();
}