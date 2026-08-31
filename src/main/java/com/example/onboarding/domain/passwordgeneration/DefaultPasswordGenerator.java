package com.example.onboarding.domain.passwordgeneration;

import java.security.SecureRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.stereotype.Component;

@Component
public class DefaultPasswordGenerator implements PasswordGenerator {
  private static final int LENGTH = 12;
  private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";

  private final SecureRandom random = new SecureRandom();

  @Override
  public String generate() {
    return IntStream.range(0, LENGTH)
        .map(i -> random.nextInt(CHARS.length()))
        .mapToObj(CHARS::charAt)
        .map(String::valueOf)
        .collect(Collectors.joining());
  }
}
