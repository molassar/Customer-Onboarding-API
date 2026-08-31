package com.example.onboarding.api.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.onboarding.domain.authentication.AuthenticatedSession;
import com.example.onboarding.domain.authentication.SessionStore;
import com.example.onboarding.domain.exception.UnauthenticatedException;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class BearerTokenAuthenticatorTest {
  private final SessionStore sessionStore = mock(SessionStore.class);
  private final BearerTokenAuthenticator authenticator =
      new BearerTokenAuthenticator(sessionStore);

  @Test
  void shouldAuthenticateBearerToken() {
    var session =
        new AuthenticatedSession(
            "access-token", Instant.parse("2026-08-31T13:00:00Z"), 42L, "john.doe");
    when(sessionStore.findByToken("access-token")).thenReturn(Optional.of(session));

    assertThat(authenticator.authenticate("Bearer access-token")).isEqualTo(session);
  }

  @Test
  void shouldRejectMissingMalformedAndUnknownTokens() {
    when(sessionStore.findByToken("unknown")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authenticator.authenticate(null))
        .isInstanceOf(UnauthenticatedException.class);
    assertThatThrownBy(() -> authenticator.authenticate("Basic credentials"))
        .isInstanceOf(UnauthenticatedException.class);
    assertThatThrownBy(() -> authenticator.authenticate("Bearer   "))
        .isInstanceOf(UnauthenticatedException.class);
    assertThatThrownBy(() -> authenticator.authenticate("Bearer unknown"))
        .isInstanceOf(UnauthenticatedException.class);
  }
}
