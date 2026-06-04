package com.example.qlbh.infrastructure.storage.minio.config;

import com.example.qlbh.infrastructure.storage.minio.MinioProperties;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MinioProperties.class)
@RequiredArgsConstructor
public class MinioConfig {

  @Bean
  public MinioClient minioClient(
      MinioProperties properties) {

    return MinioClient.builder()
        .endpoint(properties.getEndpoint())
        .credentials(
            properties.getAccessKey(),
            properties.getSecretKey())
        .build();
  }
}
