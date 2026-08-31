package com.example.onboarding.domain.account;

import java.math.BigDecimal;
import org.springframework.data.annotation.Id;

/** Account owned by a customer and persisted as part of the customer aggregate. */
public class Account {
  @Id private Long id;

  private final Iban iban;
  private final AccountType accountType;
  private final BigDecimal balance;
  private final CurrencyCode currency;

  public Account(
      Long id, Iban iban, AccountType accountType, BigDecimal balance, CurrencyCode currency) {
    this.id = id;
    this.iban = iban;
    this.accountType = accountType;
    this.balance = balance;
    this.currency = currency;
  }

  /** Opens a private euro account with a zero balance. */
  public static Account open(Iban iban) {
    return new Account(null, iban, AccountType.PRIVATE, new BigDecimal("0.00"), CurrencyCode.EUR);
  }

  public Long getId() {
    return id;
  }

  public Iban getIban() {
    return iban;
  }

  public AccountType getAccountType() {
    return accountType;
  }

  public BigDecimal getBalance() {
    return balance;
  }

  public CurrencyCode getCurrency() {
    return currency;
  }
}
