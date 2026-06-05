package com.example.qlbh.application.importjob.usecase;

public interface TriggerImportUseCase {

  // Nhận objectName, đọc file từ MinIO, import vào DB
  void triggerImport(String objectName, String fileName);
}