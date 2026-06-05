package com.example.qlbh.infrastructure.storage.minio;

import com.example.qlbh.application.importjob.port.output.FileStoragePort;
import com.example.qlbh.common.exception.BusinessException;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import java.io.InputStream;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinioStorageAdapter implements FileStoragePort {

  private final MinioClient minioClient;
  private final MinioProperties properties;

  @Override
  public String upload(MultipartFile file) {
    try {
      // objectName = uuid + original filename → tránh trùng tên
      String objectName = UUID.randomUUID() + "-" + file.getOriginalFilename();

      minioClient.putObject(
          PutObjectArgs.builder()
              .bucket(properties.getBucket())
              .object(objectName)
              .stream(file.getInputStream(), file.getSize(), -1)
              .contentType(file.getContentType())
              .build()
      );

      log.info("[MinIO] Uploaded: {} ({} bytes)", objectName, file.getSize());
      return objectName;

    } catch (Exception e) {
      log.error("[MinIO] Upload failed: {}", e.getMessage());
      throw new BusinessException("Upload file thất bại: " + e.getMessage());
    }
  }

  @Override
  public InputStream download(String objectName) {
    try {
      return minioClient.getObject(
          GetObjectArgs.builder()
              .bucket(properties.getBucket())
              .object(objectName)
              .build()
      );
    } catch (Exception e) {
      log.error("[MinIO] Download failed: {}", e.getMessage());
      throw new BusinessException("Download file thất bại: " + e.getMessage());
    }
  }
}