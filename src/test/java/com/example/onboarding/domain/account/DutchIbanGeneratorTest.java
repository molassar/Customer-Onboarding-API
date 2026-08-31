package com.example.onboarding.domain.account;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class DutchIbanGeneratorTest {

  private final IbanGenerator generator = new DutchIbanGenerator();

  @Test
  void shouldGenerateValidDutchIban() {
    IntStream.range(0, 1_000)
        .mapToObj(ignored -> generator.generate())
        .forEach(
            iban -> {
              assertThat(iban.value()).matches("NL\\d{2}BANK\\d{10}");
              assertThat(iban.value().substring(8, 11)).isNotEqualTo("099");
            });
  }
}
