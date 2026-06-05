package com.example.qlbh.infrastructure.storage.minio;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinioBucketInitializer {

  private final MinioClient minioClient;
  private final MinioProperties properties;

  // @EventListener thay vì @PostConstruct
  // → app start xong rồi mới check, không block startup
  @EventListener(ApplicationReadyEvent.class)
  public void init() {
    String bucket = properties.getBucket();
    int maxAttempts = 5;

    for (int i = 1; i <= maxAttempts; i++) {
      try {
        boolean exists = minioClient.bucketExists(
            BucketExistsArgs.builder().bucket(bucket).build()
        );
        if (!exists) {
          minioClient.makeBucket(
              MakeBucketArgs.builder().bucket(bucket).build()
          );
          log.info("[MinIO] Bucket '{}' created successfully", bucket);
        } else {
          log.info("[MinIO] Bucket '{}' already exists", bucket);
        }
        return; // thành công → thoát

      } catch (Exception e) {
        log.warn("[MinIO] Attempt {}/{} failed: {}", i, maxAttempts, e.getMessage());
        if (i < maxAttempts) {
          try {
            Thread.sleep(3000L * i);
          } // backoff: 3s, 6s, 9s...
          catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return;
          }
        } else {
          log.error("[MinIO] Cannot connect after {} attempts. " +
              "Check MinIO is running at: {}", maxAttempts, properties.getEndpoint());
        }
      }
    }
  }
}
