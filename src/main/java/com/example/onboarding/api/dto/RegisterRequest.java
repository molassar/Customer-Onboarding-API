package com.example.onboarding.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;
import java.util.Locale;

public record RegisterRequest(
    @NotBlank String username,
    @NotBlank String fullName,
    @NotNull @Valid AddressDto address,
    @NotNull @Past LocalDate dateOfBirth) {
  public RegisterRequest {
    username = username.trim().toLowerCase(Locale.ROOT);
    fullName = fullName.trim();
  }
}
