package io.forestlands.backend.controller.dto;

import io.forestlands.backend.entity.FocusSessionState;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record CompleteFocusSessionRequest(
        Instant clientEndTime,
        @NotNull FocusSessionState state
) {
}
