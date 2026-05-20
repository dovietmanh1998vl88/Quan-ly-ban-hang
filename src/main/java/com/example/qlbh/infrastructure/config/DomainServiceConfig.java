package com.example.qlbh.infrastructure.config;

import com.example.qlbh.domain.auth.service.UserDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainServiceConfig {

  @Bean
  public UserDomainService userDomainService() {
    return new UserDomainService();  // Spring quản lý bean, Domain không biết Spring
  }
}
