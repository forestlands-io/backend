package io.forestlands.backend.controller.dto;

import io.forestlands.backend.entity.FocusSessionState;

import java.time.Instant;

public record CompleteFocusSessionRequest(
        Instant clientEndTime,
        FocusSessionState state
) {
}
