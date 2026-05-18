package com.example.qlbh.infrastructure.security.jwt;

import com.example.qlbh.application.port.TokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import io.jsonwebtoken.security.Keys;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;


@Component
@RequiredArgsConstructor
public class JwtProvider implements TokenProvider {

  private final JwtProperties jwtProperties;

  private Key getSigningKey() {

    return Keys.hmacShaKeyFor(
        jwtProperties.getSecret()
            .getBytes(StandardCharsets.UTF_8)
    );
  }

  @Override
  public String generateToken(String username) {

    Date now = new Date();

    Date expiryDate = new Date(
        now.getTime() + jwtProperties.getExpiration()
    );

    return Jwts.builder()
        .setSubject(username)
        .setIssuedAt(now)
        .setExpiration(expiryDate)
        .signWith(
            getSigningKey(),
            SignatureAlgorithm.HS256
        )
        .compact();
  }

  public String extractUsername(String token) {

    return getClaims(token).getSubject();
  }

  public boolean isValidToken(String token) {

    try {

      getClaims(token);

      return true;

    } catch (Exception ex) {

      return false;
    }
  }

  private Claims getClaims(String token) {

    return Jwts.parserBuilder()
        .setSigningKey(getSigningKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
  }
}
