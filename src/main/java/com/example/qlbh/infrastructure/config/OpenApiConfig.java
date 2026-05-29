package com.example.qlbh.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("Shop Management API")
            .version("1.0")
            .description("Spring Boot Clean Architecture + DDD")
            .contact(new Contact()
                .name("Dev Team")
                .email("dev@example.com"))
        )
        // Khai báo security scheme — JWT Bearer token
        .addSecurityItem(new SecurityRequirement()
            .addList("Bearer Authentication")
        )
        .components(new Components()
            .addSecuritySchemes("Bearer Authentication",
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("Nhập JWT token vào đây")
            )
        );
  }
}