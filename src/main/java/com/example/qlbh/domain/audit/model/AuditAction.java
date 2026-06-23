package com.example.qlbh.domain.audit.model;

/**
 * AuditAction — định nghĩa tất cả action cần audit trong hệ thống.
 * <p>
 * Đặt ở Domain layer vì đây là business concept, không phải infrastructure. Mỗi module có prefix riêng → dễ filter, dễ
 * report.
 */
public enum AuditAction {

  // ===== AUTH =====
  AUTH_LOGIN,
  AUTH_LOGOUT,
  AUTH_REGISTER,
  AUTH_LOGIN_FAILED,

  // ===== PRODUCT =====
  PRODUCT_CREATE,
  PRODUCT_UPDATE,
  PRODUCT_DELETE,
  PRODUCT_STOCK_INCREASE,
  PRODUCT_STOCK_DECREASE,
  PRODUCT_IMPORT,
  PRODUCT_EXPORT,

  // ===== ORDER =====
  ORDER_CREATE,
  ORDER_CONFIRM,
  ORDER_CANCEL,
  ORDER_ITEM_ADD,
  ORDER_PRINT,

  // ===== IMPORT JOB =====
  IMPORT_UPLOAD,
  IMPORT_TRIGGER,

  // ===== USER =====
  USER_VIEW,
  USER_UPDATE,

  // ===== REPORT =====
  REPORT_REVENUE_EXPORT,

  // ===== SYSTEM =====
  SYSTEM_ERROR,
  SYSTEM_ACCESS_DENIED
}
