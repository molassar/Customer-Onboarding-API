package com.example.onboarding.api.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Locale;

public record LoginRequest(@NotBlank String username, @NotBlank String password) {
  public LoginRequest {
    if (username != null) {
      username = username.trim().toLowerCase(Locale.ROOT);
    }
  }
}
