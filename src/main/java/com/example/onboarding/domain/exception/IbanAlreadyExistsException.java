package com.example.onboarding.domain.exception;

public class IbanAlreadyExistsException extends RuntimeException {
  public IbanAlreadyExistsException(String iban, Throwable cause) {
    super("Generated IBAN already exists: " + iban, cause);
  }
}
