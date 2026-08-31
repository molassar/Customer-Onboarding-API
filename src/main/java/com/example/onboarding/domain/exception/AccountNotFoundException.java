package com.example.onboarding.domain.exception;

public class AccountNotFoundException extends RuntimeException {
  public AccountNotFoundException(Long customerId) {
    super("Account not found for customer: " + customerId);
  }
}
