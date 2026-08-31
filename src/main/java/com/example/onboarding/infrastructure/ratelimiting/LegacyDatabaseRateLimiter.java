package com.example.onboarding.infrastructure.ratelimiting;

import com.example.onboarding.infrastructure.exception.DatabaseRateLimitExceededException;
import io.github.resilience4j.ratelimiter.RateLimiter;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

@Component
public class LegacyDatabaseRateLimiter {
  private final RateLimiter rateLimiter;

  public LegacyDatabaseRateLimiter(RateLimiter rateLimiter) {
    this.rateLimiter = rateLimiter;
  }

  public <T> T execute(Supplier<T> databaseCall) {
    if (!rateLimiter.acquirePermission()) {
      throw new DatabaseRateLimitExceededException();
    }

    return databaseCall.get();
  }
}
