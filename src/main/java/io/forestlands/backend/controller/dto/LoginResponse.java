package io.forestlands.backend.controller.dto;

import java.util.UUID;

public record LoginResponse(UUID userUuid, String email) {
}
