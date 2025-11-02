package io.forestlands.backend.controller.dto;

public record LoginRequest(String email, String password, String clientId) {}
