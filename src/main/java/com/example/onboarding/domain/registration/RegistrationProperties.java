package com.example.onboarding.domain.registration;

import static com.example.onboarding.util.ArgumentChecker.checkArgument;
import static java.util.function.Predicate.not;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("onboarding.registration")
public record RegistrationProperties(Set<String> allowedCountries) {
  private static final Set<String> ISO_COUNTRIES = Set.of(Locale.getISOCountries());

  public RegistrationProperties {
    checkArgument(
        allowedCountries != null && !allowedCountries.isEmpty(),
        "At least one allowed registration country is required");

    var normalizedCountries =
        allowedCountries.stream()
            .map(country -> Objects.requireNonNull(country, "Country code must not be null"))
            .map(country -> country.trim().toUpperCase(Locale.ROOT))
            .collect(Collectors.toSet());

    Set<String> invalidCountryCodes =
        normalizedCountries.stream()
            .filter(not(ISO_COUNTRIES::contains))
            .collect(Collectors.toSet());

    checkArgument(
        invalidCountryCodes.isEmpty(),
        "Invalid allowed registration country codes: %s",
        invalidCountryCodes);

    allowedCountries = Set.copyOf(normalizedCountries);
  }

  public boolean allows(String countryCode) {
    return countryCode != null
        && allowedCountries.contains(countryCode.trim().toUpperCase(Locale.ROOT));
  }
}
