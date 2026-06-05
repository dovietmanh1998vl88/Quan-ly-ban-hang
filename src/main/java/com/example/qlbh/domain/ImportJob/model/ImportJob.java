package com.example.qlbh.domain.ImportJob.model;

import com.example.qlbh.common.enums.ImportJobStatus;
import lombok.Getter;

@Getter
public class ImportJob {

  private String id;
  private String objectName;   // key trong MinIO
  private String fileName;     // tên file gốc
  private Long totalRows;
  private Long processedRows;
  private Long failedRows;
  private ImportJobStatus status;
  private String errorMessage;

  // Constructor tạo mới
  public ImportJob(String objectName, String fileName) {
    this.objectName = objectName;
    this.fileName = fileName;
    this.status = ImportJobStatus.PENDING;
    this.totalRows = 0L;
    this.processedRows = 0L;
    this.failedRows = 0L;
  }

  // Reconstitute từ DB
  public ImportJob(String id, String objectName, String fileName,
      Long totalRows, Long processedRows, Long failedRows,
      ImportJobStatus status, String errorMessage) {
    this.id = id;
    this.objectName = objectName;
    this.fileName = fileName;
    this.totalRows = totalRows;
    this.processedRows = processedRows;
    this.failedRows = failedRows;
    this.status = status;
    this.errorMessage = errorMessage;
  }

  public void markProcessing(long totalRows) {
    this.status = ImportJobStatus.PROCESSING;
    this.totalRows = totalRows;
  }

  public void markSuccess(long processed) {
    this.status = ImportJobStatus.SUCCESS;
    this.processedRows = processed;
  }

  public void markFailed(String errorMessage) {
    this.status = ImportJobStatus.FAILED;
    this.errorMessage = errorMessage;
  }
}
