package io.forestlands.backend.controller.dto;

import java.time.Instant;
import java.util.UUID;

public record StartFocusSessionRequest(
        UUID sessionUuid,
        UUID speciesUuid,
        Instant clientStartTime,
        Integer plannedMinutes,
        String tag
) {
}
