package com.gazpacho.sharedlib.dto;

import lombok.Data;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TokenResponseDTO {
  @Builder.Default
  private final String tokenType = "Bearer";
  @NotEmpty
  private String accessToken;
  @NotEmpty
  private String refreshToken;
}
