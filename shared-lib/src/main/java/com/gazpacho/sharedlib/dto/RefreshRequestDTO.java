package com.gazpacho.sharedlib.dto;

import jakarta.validation.constraints.NotEmpty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshRequestDTO {
  @NotEmpty(message = "Refresh token cannot be empty")
  private String refreshToken;
}
