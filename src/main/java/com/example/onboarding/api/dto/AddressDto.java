package com.example.onboarding.api.dto;

import static com.example.onboarding.util.ArgumentChecker.checkArgument;

import jakarta.validation.constraints.NotBlank;
import java.util.Locale;
import java.util.Set;

public record AddressDto(
    @NotBlank String countryCode,
    @NotBlank String street,
    @NotBlank String houseNumber,
    @NotBlank String postalCode) {
  private static final Set<String> ISO_COUNTRIES = Set.of(Locale.getISOCountries());

  public AddressDto {
    countryCode = countryCode.trim().toUpperCase(Locale.ROOT);
    checkArgument(ISO_COUNTRIES.contains(countryCode), "Invalid country code: %s", countryCode);

    street = street.trim();
    houseNumber = houseNumber.trim().toUpperCase(Locale.ROOT);
    postalCode = postalCode.trim().toUpperCase(Locale.ROOT);
  }
}
