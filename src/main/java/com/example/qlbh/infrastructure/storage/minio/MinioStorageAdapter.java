package com.example.qlbh.infrastructure.storage.minio;


import com.example.qlbh.application.importjob.port.output.FileStoragePort;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import java.io.InputStream;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class MinioStorageAdapter
    implements FileStoragePort {

  private final MinioClient minioClient;

  private final MinioProperties properties;

  @Override
  public String upload(
      MultipartFile file) {

    try {

      String objectName =
          UUID.randomUUID()
              + "-"
              + file.getOriginalFilename();

      minioClient.putObject(
          PutObjectArgs.builder()
              .bucket(
                  properties.getBucket())
              .object(objectName)
              .stream(
                  file.getInputStream(),
                  file.getSize(),
                  -1)
              .contentType(
                  file.getContentType())
              .build()
      );

      return objectName;

    } catch (Exception ex) {

      throw new RuntimeException(
          "Upload MinIO thất bại",
          ex);
    }
  }

  @Override
  public InputStream download(String objectName) {
    try {

      return minioClient.getObject(
          GetObjectArgs.builder()
              .bucket(
                  properties.getBucket())
              .object(objectName)
              .build()
      );

    } catch (Exception ex) {

      throw new RuntimeException(
          "Download file thất bại",
          ex);
    }
  }
}