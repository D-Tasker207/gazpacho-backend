package com.gazpacho.sharedlib.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenResponseDTO {
  private long userId;
  private String token;
  private String tokenType;
  private long expiresIn;
}
