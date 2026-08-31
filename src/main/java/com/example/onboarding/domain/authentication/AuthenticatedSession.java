package com.example.onboarding.domain.authentication;

import java.time.Instant;

/** An authenticated customer session represented by an opaque, expiring access token. */
public record AuthenticatedSession(
    String accessToken, Instant expiresAt, Long customerId, String username) {}
