package com.gazpacho.userservice.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

import com.gazpacho.userservice.security.TokenUtils.TokenType;
import com.gazpacho.userservice.model.UserEntity;
import lombok.Getter;
import java.util.Date;
import java.security.Key;

@Component
public class TokenGenerator {
  // Access token variables

  @Getter
  @Value("${JWT_ACCESS_EXPIRATION}")
  private long accessExpTimeMillis;

  @Value("${JWT_ACCESS_SECRET}")
  private String accessSecret;
  private Key accessKey;

  // Refresh token variables

  @Getter
  @Value("${JWT_REFRESH_EXPIRATION}")
  private long refreshExpTimeMillis;

  @Value("${JWT_REFRESH_SECRET}")
  private String refreshSecret;
  private Key refreshKey;

  @PostConstruct
  public void init() {
    this.accessKey = Keys.hmacShaKeyFor(accessSecret.getBytes());
    this.refreshKey = Keys.hmacShaKeyFor(refreshSecret.getBytes());
  }

  private String generateToken(UserEntity user, TokenType type) {
    return Jwts.builder()
        .setSubject(user.getId().toString())
        .claim("type", type == TokenType.ACCESS ? "access" : "refresh")
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis()
            + (type == TokenType.ACCESS ? accessExpTimeMillis : refreshExpTimeMillis)))
        .signWith(type == TokenType.ACCESS ? accessKey : refreshKey)
        .compact();
  }

  public String generateAccessToken(UserEntity user) {
    return generateToken(user, TokenType.ACCESS);
  }

  public String generateRefreshToken(UserEntity user) {
    return generateToken(user, TokenType.REFRESH);
  }
}
