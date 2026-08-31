package com.example.onboarding.domain.exception;

public class UnauthenticatedException extends RuntimeException {
  public UnauthenticatedException() {
    super("A valid bearer token is required");
  }
}
