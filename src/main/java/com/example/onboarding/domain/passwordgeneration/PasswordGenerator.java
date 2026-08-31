package com.example.onboarding.domain.passwordgeneration;

/** Generates initial passwords for newly registered customers. */
public interface PasswordGenerator {
  /** Returns a newly generated password in plain text for one-time delivery to the customer. */
  String generate();
}
