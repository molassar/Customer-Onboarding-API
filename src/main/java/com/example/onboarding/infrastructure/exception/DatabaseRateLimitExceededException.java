package com.example.onboarding.infrastructure.exception;

public class DatabaseRateLimitExceededException extends RuntimeException {
  public DatabaseRateLimitExceededException() {
    super("The database request limit has been reached, please try again later");
  }
}
