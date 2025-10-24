package io.forestlands.backend.controller.dto;

import io.forestlands.backend.entity.FocusSessionState;

import java.time.Instant;
import java.util.UUID;

public record StartFocusSessionResponse(
        Long id,
        UUID sessionUuid,
        Instant serverStartTime,
        FocusSessionState state
) {
}
