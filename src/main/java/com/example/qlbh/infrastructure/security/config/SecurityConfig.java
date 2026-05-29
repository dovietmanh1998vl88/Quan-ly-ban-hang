package com.example.qlbh.infrastructure.security.config;

import com.example.qlbh.common.response.BaseResponse;
import com.example.qlbh.infrastructure.security.filter.JwtAuthenticationFilter;
import com.example.qlbh.infrastructure.security.handler.CustomAccessDeniedHandler;
import com.example.qlbh.infrastructure.security.handler.CustomAuthenticationEntryPoint;
import com.example.qlbh.infrastructure.security.service.CustomUserDetailsService;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;

import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  private final CustomUserDetailsService customUserDetailsService;

  private final CustomAuthenticationEntryPoint authenticationEntryPoint;

  private final ObjectMapper objectMapper;

  private final CustomAccessDeniedHandler accessDeniedHandler;

  @Bean
  public PasswordEncoder passwordEncoder() {

    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationProvider authenticationProvider() {

    DaoAuthenticationProvider provider =
        new DaoAuthenticationProvider();

    provider.setUserDetailsService(
        customUserDetailsService
    );

    provider.setPasswordEncoder(
        passwordEncoder()
    );

    return provider;
  }

  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration configuration
  ) throws Exception {

    return configuration.getAuthenticationManager();
  }

  // infrastructure/security/config/SecurityConfig.java
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

    http
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .authorizeHttpRequests(auth -> auth

            // Public — không cần token
            .requestMatchers("/auth/**").permitAll()
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

            // Product — phân quyền chi tiết
            .requestMatchers(HttpMethod.GET, "/products/**").hasAnyRole("ADMIN", "STAFF", "CUSTOMER")
            .requestMatchers(HttpMethod.POST, "/products").hasRole("ADMIN")
            .requestMatchers(HttpMethod.PUT, "/products/*/update-product").hasRole("ADMIN")
            .requestMatchers(HttpMethod.PUT, "/products/*/stock").hasAnyRole("ADMIN", "STAFF")
            .requestMatchers(HttpMethod.DELETE, "/products/**").hasRole("ADMIN")

            .anyRequest().authenticated()
        ).exceptionHandling(exception -> exception
            .authenticationEntryPoint(authenticationEntryPoint) // 401
            .accessDeniedHandler(accessDeniedHandler)           // 403
        )
//        .exceptionHandling(exception -> exception
//            .authenticationEntryPoint(authenticationEntryPoint)
//            // Thêm AccessDeniedHandler — trả 403 JSON thay vì trang trắng
//            .accessDeniedHandler(accessDeniedHandler())
//        )
        .authenticationProvider(authenticationProvider())
        .addFilterBefore(jwtAuthenticationFilter,
            UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  // 403 handler — tương tự CustomAuthenticationEntryPoint
  @Bean
  public AccessDeniedHandler accessDeniedHandler() {
    return (request, response, ex) -> {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.getWriter().write(
          objectMapper.writeValueAsString(
              BaseResponse.error("Bạn không có quyền thực hiện thao tác này")
          )
      );
    };
  }
}