package com.example.onboarding.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import com.example.onboarding.domain.registration.RegistrationProperties;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RegistrationPropertiesTest {

  @Test
  void shouldAllowConfiguredCountries() {
    var properties = new RegistrationProperties(Set.of("nl", "BE", "de"));

    assertThat(properties.allowedCountries()).containsExactlyInAnyOrder("BE", "DE", "NL");
    assertThat(properties.allows("DE")).isTrue();
  }

  @Test
  void shouldRejectInvalidConfiguredCountry() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new RegistrationProperties(Set.of("NL", "XX")))
        .withMessage("Invalid allowed registration country codes: [XX]");
  }
}
