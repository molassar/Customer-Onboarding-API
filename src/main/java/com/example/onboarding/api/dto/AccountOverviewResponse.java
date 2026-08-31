package com.example.onboarding.api.dto;

import com.example.onboarding.domain.account.AccountType;
import com.example.onboarding.domain.account.CurrencyCode;
import java.math.BigDecimal;

public record AccountOverviewResponse(
    String accountNumber, AccountType accountType, BigDecimal balance, CurrencyCode currency) {}
