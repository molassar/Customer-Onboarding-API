package com.example.onboarding.infrastructure.ratelimiting;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.onboarding.infrastructure.exception.DatabaseRateLimitExceededException;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class LegacyDatabaseRateLimiterTest {

  @Test
  void shouldRejectDatabaseCallsAboveConfiguredLimit() {
    var config =
        RateLimiterConfig.custom()
            .limitForPeriod(2)
            .limitRefreshPeriod(Duration.ofSeconds(1))
            .timeoutDuration(Duration.ZERO)
            .build();
    var limiter = new LegacyDatabaseRateLimiter(RateLimiter.of("testDatabase", config));

    limiter.execute(() -> "first");
    limiter.execute(() -> "second");
    assertThatThrownBy(() -> limiter.execute(() -> "third"))
        .isInstanceOf(DatabaseRateLimitExceededException.class);
  }
}
