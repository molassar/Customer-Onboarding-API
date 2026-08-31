package com.example.onboarding.domain.exception;

public class UsernameAlreadyExistsException extends RuntimeException {
  public UsernameAlreadyExistsException(String message, Throwable cause) {
    super(message, cause);
  }
}
