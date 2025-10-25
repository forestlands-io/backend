package io.forestlands.backend.controller.dto;

import jakarta.validation.constraints.*;

import java.time.Instant;
import java.util.UUID;

public record StartFocusSessionRequest(
        @NotNull UUID sessionUuid,
        @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "speciesCode must be alphanumeric with underscores")
        @Size(max = 64, message = "speciesCode must be at most 64 characters")
        String speciesCode,
        @NotNull Instant clientStartTime,
        @Min(value = 5, message = "plannedMinutes must be at least 5")
        @Max(value = 120, message = "plannedMinutes must be at most 120")
        Integer plannedMinutes,
        @Pattern(regexp = "^[A-Za-z0-9 ]+$", message = "tag can contain only letters, numbers, and spaces")
        @Size(max = 20, message = "tag must be at most 20 characters")
        String tag
) {
}
