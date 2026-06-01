// common/util/SecurityUtils.java
package com.example.qlbh.common.util;

import com.example.qlbh.common.exception.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Utility lấy thông tin user đang login từ Keycloak JWT.
 * <p>
 * Keycloak JWT có nhiều thông tin hơn JWT tự làm:
 * - sub:                UUID của user trong Keycloak
 * - preferred_username: username
 * - email:              email
 * - realm_access.roles: roles
 */
@Slf4j
public final class SecurityUtils {

  private SecurityUtils() {
  }

  /**
   * Lấy username (preferred_username) từ Keycloak JWT.
   */
  public static String getCurrentUsername() {
    return getJwt().getClaimAsString("preferred_username");
  }

  /**
   * Lấy Keycloak user ID (sub claim = UUID). Dùng khi cần link với data của user.
   */
  public static String getCurrentUserId() {
    return getJwt().getSubject();
  }

  /**
   * Lấy email từ Keycloak JWT.
   */
  public static String getCurrentEmail() {
    return getJwt().getClaimAsString("email");
  }

  /**
   * Lấy toàn bộ JWT object — dùng khi cần claim đặc biệt.
   */
  public static Jwt getJwt() {
    Authentication auth = SecurityContextHolder
        .getContext()
        .getAuthentication();

    if (auth == null || !auth.isAuthenticated()) {
      throw new UnauthorizedException("Chưa đăng nhập");
    }

    if (!(auth instanceof JwtAuthenticationToken jwtAuth)) {
      throw new UnauthorizedException("Token không hợp lệ");
    }

    return jwtAuth.getToken();
  }
}