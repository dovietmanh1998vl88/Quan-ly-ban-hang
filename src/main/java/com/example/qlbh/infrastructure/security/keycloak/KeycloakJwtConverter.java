// infrastructure/security/keycloak/KeycloakJwtConverter.java
package com.example.qlbh.infrastructure.security.keycloak;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Convert JWT Keycloak → Spring Security Authentication.
 * <p>
 * JWT từ Keycloak có structure: { "sub":                "uuid-keycloak-user-id", "preferred_username": "admin_kc",
 * "email":              "admin@shop.com", "realm_access": { "roles": ["ADMIN", "offline_access", "uma_authorization"] }
 * }
 * <p>
 * Spring Security cần:
 * - Principal name: preferred_username
 * - Authorities: [ROLE_ADMIN]
 */
@Slf4j
@Component
public class KeycloakJwtConverter
    implements Converter<Jwt, AbstractAuthenticationToken> {

  // Roles hợp lệ của app — lọc bỏ roles nội bộ Keycloak
  private static final List<String> APP_ROLES =
      List.of("ADMIN", "STAFF", "CUSTOMER");

  @Override
  public AbstractAuthenticationToken convert(Jwt jwt) {
    Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
    String username = extractUsername(jwt);

    log.debug("Keycloak JWT converted — user: {}, authorities: {}",
        username, authorities);

    return new JwtAuthenticationToken(jwt, authorities, username);
  }

  /**
   * Extract username từ preferred_username claim. Keycloak luôn có claim này khi config đúng.
   */
  private String extractUsername(Jwt jwt) {
    String username = jwt.getClaimAsString("preferred_username");
    if (username == null) {
      // Fallback về sub (UUID) nếu không có preferred_username
      username = jwt.getSubject();
    }
    return username;
  }

  /**
   * Extract roles từ realm_access.roles. Chỉ lấy roles của app, bỏ roles nội bộ Keycloak như offline_access,
   * uma_authorization.
   */
  @SuppressWarnings("unchecked")
  private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
    Map<String, Object> realmAccess =
        jwt.getClaimAsMap("realm_access");

    if (realmAccess == null) {
      log.warn("JWT không có claim realm_access — user: {}",
          jwt.getSubject());
      return Collections.emptyList();
    }

    List<String> roles =
        (List<String>) realmAccess.getOrDefault("roles", List.of());

    return roles.stream()
        .filter(APP_ROLES::contains)        // chỉ lấy role của app
        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
        .collect(Collectors.toList());
  }
}