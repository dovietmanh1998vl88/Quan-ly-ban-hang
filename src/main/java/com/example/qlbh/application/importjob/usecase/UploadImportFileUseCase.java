package com.example.qlbh.application.importjob.usecase;

import org.springframework.web.multipart.MultipartFile;

public interface UploadImportFileUseCase {

  String upload(MultipartFile file);
}
