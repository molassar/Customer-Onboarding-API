package com.example.onboarding.domain.account;

/** Generates valid account numbers for newly opened accounts. */
public interface IbanGenerator {
  /** Returns a newly generated IBAN candidate. */
  Iban generate();
}
