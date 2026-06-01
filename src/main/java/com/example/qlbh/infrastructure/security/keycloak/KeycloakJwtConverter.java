// infrastructure/security/keycloak/KeycloakJwtConverter.java
package com.example.qlbh.infrastructure.security.keycloak;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Convert JWT từ Keycloak sang Spring Security Authentication.
 * <p>
 * Keycloak lưu roles tại: realm_access.roles Spring Security cần: Collection<GrantedAuthority>
 * <p>
 * Ví dụ JWT payload từ Keycloak: { "realm_access": { "roles": ["ADMIN", "offline_access"] }, "preferred_username":
 * "admin" }
 * <p>
 * → Convert thành ROLE_ADMIN để hasRole("ADMIN") hoạt động
 */
@Component
public class KeycloakJwtConverter
    implements Converter<Jwt, AbstractAuthenticationToken> {

  @Override
  public AbstractAuthenticationToken convert(Jwt jwt) {
    Collection<GrantedAuthority> authorities = extractRoles(jwt);

    // preferred_username là username trong Keycloak
    String username = jwt.getClaimAsString("preferred_username");

    return new JwtAuthenticationToken(jwt, authorities, username);
  }

  /**
   * Extract roles từ claim realm_access.roles Thêm prefix ROLE_ để hasRole() hoạt động
   */
  @SuppressWarnings("unchecked")
  private Collection<GrantedAuthority> extractRoles(Jwt jwt) {
    // Lấy map realm_access từ JWT
    Map<String, Object> realmAccess =
        jwt.getClaimAsMap("realm_access");

    if (realmAccess == null || !realmAccess.containsKey("roles")) {
      return List.of();
    }

    List<String> roles = (List<String>) realmAccess.get("roles");

    return roles.stream()
        // Chỉ lấy role của app, bỏ role nội bộ của Keycloak
        .filter(role -> role.equals("ADMIN")
            || role.equals("STAFF")
            || role.equals("CUSTOMER"))
        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
        .collect(Collectors.toList());
  }
}