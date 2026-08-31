package com.example.onboarding.domain.account;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class DutchIbanGenerator implements IbanGenerator {
  private static final String BANK_CODE = "BANK";
  private static final String RESERVED_G_ACCOUNT_PREFIX = "099";
  private static final long ACCOUNT_NUMBER_BOUND = 10_000_000_000L;

  private final SecureRandom random = new SecureRandom();

  @Override
  public Iban generate() {
    String accountNumber;

    do {
      accountNumber = "%010d".formatted(random.nextLong(ACCOUNT_NUMBER_BOUND));
    } while (accountNumber.startsWith(RESERVED_G_ACCOUNT_PREFIX));

    return Iban.dutch(BANK_CODE, accountNumber);
  }
}
