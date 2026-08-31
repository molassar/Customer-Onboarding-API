package com.example.onboarding.domain.exception;

public class UnderageCustomerException extends RuntimeException {
  public UnderageCustomerException() {
    super("Customers must be at least 18 years old to register");
  }
}
