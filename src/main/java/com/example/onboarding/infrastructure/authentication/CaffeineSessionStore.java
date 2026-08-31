package com.example.onboarding.infrastructure.authentication;

import com.example.onboarding.config.LoginSessionProperties;
import com.example.onboarding.domain.authentication.AuthenticatedSession;
import com.example.onboarding.domain.authentication.SessionStore;
import com.github.benmanes.caffeine.cache.Cache;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class CaffeineSessionStore implements SessionStore {
  private static final int TOKEN_BYTES = 32;

  private final Cache<String, AuthenticatedSession> sessions;
  private final LoginSessionProperties properties;
  private final Clock clock;
  private final SecureRandom secureRandom;

  public CaffeineSessionStore(
      Cache<String, AuthenticatedSession> sessions,
      LoginSessionProperties properties,
      Clock clock,
      SecureRandom secureRandom) {
    this.sessions = sessions;
    this.properties = properties;
    this.clock = clock;
    this.secureRandom = secureRandom;
  }

  @Override
  public AuthenticatedSession create(Long customerId, String username) {
    Instant expiresAt = Instant.now(clock).plus(properties.sessionTtl());

    while (true) {
      String accessToken = generateToken();
      var session = new AuthenticatedSession(accessToken, expiresAt, customerId, username);

      if (sessions.asMap().putIfAbsent(accessToken, session) == null) {
        return session;
      }
    }
  }

  @Override
  public Optional<AuthenticatedSession> findByToken(String accessToken) {
    return Optional.ofNullable(sessions.getIfPresent(accessToken));
  }

  private String generateToken() {
    byte[] token = new byte[TOKEN_BYTES];
    secureRandom.nextBytes(token);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(token);
  }
}
