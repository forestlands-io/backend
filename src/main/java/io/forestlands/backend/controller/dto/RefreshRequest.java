package io.forestlands.backend.controller.dto;

public record RefreshRequest(String refreshToken, String clientId) { }
