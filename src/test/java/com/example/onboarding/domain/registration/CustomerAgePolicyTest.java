package com.example.onboarding.domain.registration;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.onboarding.domain.exception.UnderageCustomerException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class CustomerAgePolicyTest {
  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2026-08-31T12:00:00Z"), ZoneOffset.UTC);

  private final CustomerAgePolicy policy = new CustomerAgePolicy(CLOCK);

  @Test
  void shouldAllowCustomerOnEighteenthBirthday() {
    assertThatCode(() -> policy.verify(LocalDate.of(2008, 8, 31))).doesNotThrowAnyException();
  }

  @Test
  void shouldRejectCustomerBeforeEighteenthBirthday() {
    assertThatThrownBy(() -> policy.verify(LocalDate.of(2008, 9, 1)))
        .isInstanceOf(UnderageCustomerException.class);
  }
}
