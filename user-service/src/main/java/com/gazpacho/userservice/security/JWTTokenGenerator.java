package com.gazpacho.userservice.security;

import com.gazpacho.userservice.model.UserEntity;

import lombok.Getter;

public class JWTTokenGenerator implements TokenGenerator {
  @Getter
  private final long expirationTimeMillis;
  private final String secretKey;

  public JWTTokenGenerator() {
    // FIXME: Pull these fields from the .env file
    secretKey = "";
    expirationTimeMillis = 0;
  }

  public String generateToken(UserEntity user) {
    return null;
  }
}
