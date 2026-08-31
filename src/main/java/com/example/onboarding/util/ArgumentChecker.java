package com.example.onboarding.util;

public class ArgumentChecker {
  private ArgumentChecker() {}

  public static void checkArgument(boolean condition, String message) {
    if (!condition) {
      throw new IllegalArgumentException(message);
    }
  }

  public static void checkArgument(boolean condition, String template, Object... args) {
    if (!condition) {
      throw new IllegalArgumentException(String.format(template, args));
    }
  }
}
