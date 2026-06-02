package com.example.qlbh.common.enums;

public enum OrderStatus {
  DRAFT,      // đang tạo, chưa confirm
  CONFIRMED,  // đã xác nhận, stock đã trừ
  SHIPPED,    // đang giao
  DELIVERED,  // đã giao thành công
  CANCELLED   // đã hủy
}