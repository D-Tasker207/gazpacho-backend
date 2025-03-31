package com.gazpacho.userservice.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

import com.gazpacho.userservice.model.UserEntity;
import lombok.Getter;
import java.util.Date;
import java.security.Key;

@Component
public class TokenGenerator {
  @Getter
  @Value("${JWT_EXPIRATION}")
  private long expirationTimeMillis;

  @Value("${JWT_SECRET}")
  private String secretKey;
  private Key key;

  @PostConstruct
  public void init() {
    this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
  }

  public String generateToken(UserEntity user) {
    return Jwts.builder()
        .setSubject(user.getId().toString())
        .claim("email", user.getEmail())
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + expirationTimeMillis))
        .signWith(key)
        .compact();
  }

}
