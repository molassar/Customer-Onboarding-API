package com.example.onboarding.config;

import static com.example.onboarding.util.ArgumentChecker.checkArgument;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("onboarding.login")
public record LoginSessionProperties(Duration sessionTtl, long maximumSessions) {
  public LoginSessionProperties {
    checkArgument(
        sessionTtl != null && sessionTtl.isPositive(), "Login session TTL must be positive");
    checkArgument(maximumSessions > 0, "Maximum login sessions must be positive");
  }
}
