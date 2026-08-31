package com.example.onboarding.domain.registration;

import com.example.onboarding.domain.exception.UnderageCustomerException;
import java.time.Clock;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

/** Enforces the minimum registration age using an injectable clock. */
@Component
public class CustomerAgePolicy {
  static final int MINIMUM_AGE = 18;

  private final Clock clock;

  public CustomerAgePolicy(Clock clock) {
    this.clock = clock;
  }

  /**
   * Verifies that the customer is at least {@value #MINIMUM_AGE} years old today.
   *
   * @throws UnderageCustomerException when the customer has not reached the minimum age
   */
  public void verify(LocalDate dateOfBirth) {
    LocalDate minimumAgeDate = dateOfBirth.plusYears(MINIMUM_AGE);
    if (minimumAgeDate.isAfter(LocalDate.now(clock))) {
      throw new UnderageCustomerException();
    }
  }
}
