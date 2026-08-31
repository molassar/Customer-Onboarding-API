package com.example.onboarding.api.authentication;

import com.example.onboarding.domain.authentication.AuthenticatedSession;
import com.example.onboarding.domain.authentication.SessionStore;
import com.example.onboarding.domain.exception.UnauthenticatedException;
import org.springframework.stereotype.Component;

/** Resolves HTTP bearer credentials to an active authenticated session. */
@Component
public class BearerTokenAuthenticator {
  private static final String BEARER_PREFIX = "Bearer ";

  private final SessionStore sessionStore;

  public BearerTokenAuthenticator(SessionStore sessionStore) {
    this.sessionStore = sessionStore;
  }

  /**
   * Validates a bearer header and looks up its session.
   *
   * @throws UnauthenticatedException when the header or token is missing, malformed, or unknown
   */
  public AuthenticatedSession authenticate(String authorizationHeader) {
    if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
      throw new UnauthenticatedException();
    }

    String accessToken = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
    if (accessToken.isEmpty()) {
      throw new UnauthenticatedException();
    }

    return sessionStore.findByToken(accessToken).orElseThrow(UnauthenticatedException::new);
  }
}
