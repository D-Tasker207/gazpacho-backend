package com.gazpacho.sharedlib.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginDTO {
  @NotBlank(message = "Email is required")
  @Email(message = "Must be a valid email address")
  private String email;

  @NotBlank(message = "Password is required")
  @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters")
  private String password;
}
