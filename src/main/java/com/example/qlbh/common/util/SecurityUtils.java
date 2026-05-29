package com.example.qlbh.common.util;

import com.example.qlbh.common.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

// common/util/SecurityUtils.java
public final class SecurityUtils {

  private SecurityUtils() {
  }

  /**
   * Lấy username của user đang login từ SecurityContext. SecurityContext được set bởi JwtAuthenticationFilter mỗi
   * request.
   */
  public static String getCurrentUsername() {
    Authentication auth = SecurityContextHolder
        .getContext()
        .getAuthentication();

    if (auth == null || !auth.isAuthenticated()) {
      throw new UnauthorizedException("Chưa đăng nhập");
    }

    return auth.getName(); // username
  }
}
