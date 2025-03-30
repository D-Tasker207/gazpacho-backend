package com.gazpacho.userservice.security;

import com.gazpacho.userservice.model.UserEntity;

public interface TokenGenerator {

  /**
   * Generate a verification token for the provided user
   *
   * @param UserEntity instance for the given user
   * @return a token string
   */
  public String generateToken(UserEntity user);
}
