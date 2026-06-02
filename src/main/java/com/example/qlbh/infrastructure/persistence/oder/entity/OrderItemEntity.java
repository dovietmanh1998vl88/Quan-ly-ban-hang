package com.example.qlbh.infrastructure.persistence.oder.entity;

import com.example.qlbh.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

// infrastructure/persistence/order/entity/OrderItemEntity.java
@Getter
@Setter
@Entity
@Table(name = "order_items")
public class OrderItemEntity extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private OrderEntity order;

  @Column(nullable = false)
  private String productId;

  @Column(nullable = false)
  private String productName;

  @Column(nullable = false)
  private Integer quantity;

  @Column(nullable = false, precision = 15, scale = 2)
  private BigDecimal unitPrice;
}
