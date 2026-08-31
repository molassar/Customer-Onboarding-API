package com.example.onboarding.domain.account;

import static com.example.onboarding.util.ArgumentChecker.checkArgument;

import java.util.Locale;
import java.util.regex.Pattern;

/** Validated Dutch IBAN value object. */
public record Iban(String value) {
  private static final Pattern DUTCH_IBAN_PATTERN = Pattern.compile("NL\\d{2}[A-Z]{4}\\d{10}");

  public Iban {
    checkArgument(value != null, "IBAN must not be null");
    value = value.replace(" ", "").toUpperCase(Locale.ROOT);
    checkArgument(DUTCH_IBAN_PATTERN.matcher(value).matches(), "Invalid Dutch IBAN: %s", value);
    checkArgument(mod97(value.substring(4) + value.substring(0, 4)) == 1, "Invalid IBAN checksum");
  }

  /**
   * Creates a Dutch IBAN and calculates its ISO 13616 check digits.
   *
   * @param bankCode four uppercase letters identifying the bank
   * @param accountNumber a ten-digit domestic account number
   */
  public static Iban dutch(String bankCode, String accountNumber) {
    checkArgument(
        bankCode != null && bankCode.matches("[A-Z]{4}"), "Invalid Dutch bank code: %s", bankCode);
    checkArgument(
        accountNumber != null && accountNumber.matches("\\d{10}"),
        "Invalid Dutch account number: %s",
        accountNumber);

    String bban = bankCode + accountNumber;
    int checkDigits = 98 - mod97(bban + "NL00");

    return new Iban("NL%02d%s".formatted(checkDigits, bban));
  }

  private static int mod97(String value) {
    int remainder = 0;

    for (char character : value.toCharArray()) {
      if (Character.isDigit(character)) {
        remainder = (remainder * 10 + Character.digit(character, 10)) % 97;
      } else {
        int numericValue = character - 'A' + 10;
        remainder = (remainder * 100 + numericValue) % 97;
      }
    }

    return remainder;
  }
}
