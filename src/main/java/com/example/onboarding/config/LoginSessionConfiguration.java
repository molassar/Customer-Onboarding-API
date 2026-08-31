package com.example.onboarding.config;

import com.example.onboarding.domain.authentication.AuthenticatedSession;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.security.SecureRandom;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoginSessionConfiguration {

  @Bean
  Cache<String, AuthenticatedSession> authenticatedSessionCache(LoginSessionProperties properties) {
    return Caffeine.newBuilder()
        .expireAfterWrite(properties.sessionTtl())
        .maximumSize(properties.maximumSessions())
        .build();
  }

  @Bean
  SecureRandom secureRandom() {
    return new SecureRandom();
  }
}
