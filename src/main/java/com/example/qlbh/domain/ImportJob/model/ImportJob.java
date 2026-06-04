package com.example.qlbh.domain.ImportJob.model;

import com.example.qlbh.common.enums.ImportJobStatus;

public class ImportJob {

  private String id;

  private String objectName;

  private Long totalRows;

  private Long processedRows;

  private ImportJobStatus status;
}
