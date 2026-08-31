package com.example.onboarding.infrastructure.authentication;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.onboarding.config.LoginSessionProperties;
import com.example.onboarding.domain.authentication.AuthenticatedSession;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class CaffeineSessionStoreTest {
  private static final Instant NOW = Instant.parse("2026-08-31T12:00:00Z");

  @Test
  void shouldCreateAOneHourSessionWithASecureOpaqueToken() {
    var fixture = fixture(10);

    AuthenticatedSession session = fixture.store().create(42L, "john.doe");

    assertThat(session.accessToken()).matches("[A-Za-z0-9_-]{43}");
    assertThat(session.expiresAt()).isEqualTo(NOW.plus(Duration.ofHours(1)));
    assertThat(fixture.store().findByToken(session.accessToken())).contains(session);
  }

  @Test
  void shouldExpireSessionsAfterOneHour() {
    var fixture = fixture(10);
    AuthenticatedSession session = fixture.store().create(42L, "john.doe");

    fixture.ticker().advance(Duration.ofHours(1));

    assertThat(fixture.store().findByToken(session.accessToken())).isEmpty();
  }

  @Test
  void shouldBoundTheNumberOfSessions() {
    var fixture = fixture(2);

    fixture.store().create(1L, "first");
    fixture.store().create(2L, "second");
    fixture.store().create(3L, "third");
    fixture.cache().cleanUp();

    assertThat(fixture.cache().estimatedSize()).isLessThanOrEqualTo(2);
  }

  private static Fixture fixture(long maximumSessions) {
    var ticker = new MutableTicker();
    var properties = new LoginSessionProperties(Duration.ofHours(1), maximumSessions);
    var cache =
        Caffeine.newBuilder()
            .expireAfterWrite(properties.sessionTtl())
            .maximumSize(properties.maximumSessions())
            .ticker(ticker)
            .<String, AuthenticatedSession>build();
    var store =
        new CaffeineSessionStore(
            cache, properties, Clock.fixed(NOW, ZoneOffset.UTC), new SecureRandom());
    return new Fixture(store, cache, ticker);
  }

  private record Fixture(
      CaffeineSessionStore store,
      com.github.benmanes.caffeine.cache.Cache<String, AuthenticatedSession> cache,
      MutableTicker ticker) {}

  private static final class MutableTicker implements Ticker {
    private final AtomicLong nanoseconds = new AtomicLong();

    @Override
    public long read() {
      return nanoseconds.get();
    }

    void advance(Duration duration) {
      nanoseconds.addAndGet(duration.toNanos());
    }
  }
}
