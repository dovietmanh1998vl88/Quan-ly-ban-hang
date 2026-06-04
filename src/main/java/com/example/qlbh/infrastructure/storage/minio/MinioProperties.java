package com.example.qlbh.infrastructure.storage.minio;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@ConfigurationProperties(prefix = "minio")
@Getter
@Setter
public class MinioProperties {

  private String endpoint;

  private String accessKey;

  private String secretKey;

  private String bucket;
}
