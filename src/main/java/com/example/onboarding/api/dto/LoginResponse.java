package com.example.onboarding.api.dto;

import java.time.Instant;

public record LoginResponse(String accessToken, Instant expiresAt) {}
