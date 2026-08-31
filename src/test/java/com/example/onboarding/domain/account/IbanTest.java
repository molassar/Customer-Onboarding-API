package com.example.onboarding.domain.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;

class IbanTest {

  @Test
  void shouldCalculateDutchIbanCheckDigits() {
    var iban = Iban.dutch("BANK", "0417164300");

    assertThat(iban.value()).isEqualTo("NL04BANK0417164300");
  }

  @Test
  void shouldRejectInvalidChecksum() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new Iban("NL00BANK0417164300"))
        .withMessage("Invalid IBAN checksum");
  }
}
