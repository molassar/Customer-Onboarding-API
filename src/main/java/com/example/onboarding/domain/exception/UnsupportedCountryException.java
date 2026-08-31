package com.example.onboarding.domain.exception;

import java.util.Set;

public class UnsupportedCountryException extends RuntimeException {
  public UnsupportedCountryException(String countryCode, Set<String> allowedCountries) {
    super(
        "Registration is only available for countries [%s], received: %s"
            .formatted(String.join(", ", allowedCountries), countryCode));
  }
}
