package io.forestlands.backend.controller.dto;

public record TokenResponse(
        String accessToken,
        long expiresIn,          // seconds until access token expiry
        String refreshToken,
        String tokenType         // "Bearer"
) { }
