package com.example.qlbh.infrastructure.storage.minio;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MinioBucketInitializer {

  private final MinioClient minioClient;
  private final MinioProperties properties;

  @PostConstruct
  public void createBucketIfNotExists() throws Exception {

    String bucket = properties.getBucket();

    boolean exists =
        minioClient.bucketExists(
            BucketExistsArgs.builder()
                .bucket(bucket)
                .build());

    if (!exists) {
      minioClient.makeBucket(
          MakeBucketArgs.builder()
              .bucket(bucket)
              .build());

      System.out.println("Created bucket: " + bucket);
    }
  }
}
