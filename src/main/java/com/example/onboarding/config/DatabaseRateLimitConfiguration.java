package com.example.onboarding.config;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseRateLimitConfiguration {

  @Bean
  RateLimiter databaseRateLimiter(DatabaseRateLimitProperties properties) {
    var config =
        RateLimiterConfig.custom()
            .limitForPeriod(properties.limitForPeriod())
            .limitRefreshPeriod(properties.limitRefreshPeriod())
            .timeoutDuration(Duration.ZERO)
            .build();

    return RateLimiter.of("legacyDatabase", config);
  }
}
