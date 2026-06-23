package com.example.qlbh.domain.audit.model;

public enum AuditStatus {
  SUCCESS,
  FAILED,
  PARTIAL   // một phần thành công (ví dụ import 90/100 dòng)
}