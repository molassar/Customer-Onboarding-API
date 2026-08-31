package com.example.onboarding.domain.authentication;

import java.util.Optional;

/** Port for creating and resolving short-lived authenticated sessions. */
public interface SessionStore {
  /** Creates a session for the identified customer. */
  AuthenticatedSession create(Long customerId, String username);

  /** Finds a non-expired session by its opaque access token. */
  Optional<AuthenticatedSession> findByToken(String accessToken);
}
