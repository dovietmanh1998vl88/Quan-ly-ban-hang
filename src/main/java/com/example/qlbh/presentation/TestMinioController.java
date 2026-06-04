package com.example.qlbh.presentation;

import com.example.qlbh.application.importjob.port.output.FileStoragePort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
public class TestMinioController {

  private final FileStoragePort
      fileStoragePort;

  @PostMapping("/upload")
  @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
  public String upload(
      @RequestParam("file")
      MultipartFile file) {

    return fileStoragePort.upload(file);
  }
}
