package com.example.onboarding.domain.customer;

/** Postal address supplied by a customer during registration. */
public record Address(String countryCode, String street, String houseNumber, String postalCode) {}
