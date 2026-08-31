package com.example.onboarding.domain.account;

import java.math.BigDecimal;

/** Read model containing the account information exposed to an authenticated customer. */
public record AccountOverview(
    Iban accountNumber, AccountType accountType, BigDecimal balance, CurrencyCode currency) {}
