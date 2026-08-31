package com.example.onboarding.config;

import static com.example.onboarding.util.ArgumentChecker.checkArgument;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("onboarding.database-rate-limit")
public record DatabaseRateLimitProperties(int limitForPeriod, Duration limitRefreshPeriod) {

  public DatabaseRateLimitProperties {
    checkArgument(limitForPeriod > 0, "Database rate limit must allow at least one request");
    checkArgument(
        limitRefreshPeriod != null && limitRefreshPeriod.isPositive(),
        "Database rate limit refresh period must be positive");
  }
}
