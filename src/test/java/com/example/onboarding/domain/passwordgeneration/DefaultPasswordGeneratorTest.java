package com.example.onboarding.domain.passwordgeneration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class DefaultPasswordGeneratorTest {
  private final PasswordGenerator generator = new DefaultPasswordGenerator();

  @Test
  void shouldGenerateTwelveCharacterPasswordFromAllowedAlphabet() {
    IntStream.range(0, 1_000)
        .mapToObj(ignored -> generator.generate())
        .forEach(password -> assertThat(password).hasSize(12).matches("[A-HJ-NP-Za-km-z2-9]{12}"));
  }
}
