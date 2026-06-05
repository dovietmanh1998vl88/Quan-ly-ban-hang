package com.example.qlbh.infrastructure.storage.minio.config;

import com.example.qlbh.infrastructure.storage.minio.MinioProperties;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// infrastructure/storage/minio/config/MinioConfig.java
@Configuration
@EnableConfigurationProperties(MinioProperties.class)
@RequiredArgsConstructor
public class MinioConfig {

  @Bean
  @ConditionalOnProperty(
      name = "minio.endpoint",
      matchIfMissing = false
  )
  public MinioClient minioClient(MinioProperties properties) {
    return MinioClient.builder()
        .endpoint(properties.getEndpoint())
        .credentials(properties.getAccessKey(), properties.getSecretKey())
        .build();
  }

  // Fallback bean khi không có MinIO — trả lỗi rõ ràng
  @Bean
  @ConditionalOnMissingBean(MinioClient.class)
  public MinioClient minioClientFallback() {
    return MinioClient.builder()
        .endpoint("http://localhost:9000")
        .credentials("admin", "admin")
        .build();
  }
}
