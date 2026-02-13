package io.forestlands.backend.controller.dto;

import io.forestlands.backend.entity.FocusSessionState;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserMeResponse(
        List<SessionItem> sessions,
        List<InventoryItem> inventory,
        List<String> unlockedSpeciesCodes,
        WalletSummary wallet
) {
    public record SessionItem(
            UUID sessionUuid,
            FocusSessionState state,
            String speciesCode,
            String speciesName,
            Integer plannedMinutes,
            Integer durationMinutes,
            String tag,
            Instant clientStartTime,
            Instant clientEndTime,
            Instant serverStartTime,
            Instant serverEndTime
    ) {
    }

    public record InventoryItem(
            Long id,
            UUID speciesUuid,
            String speciesCode,
            String speciesName,
            boolean placed,
            Integer cellX,
            Integer cellY
    ) {
    }

    public record WalletSummary(
            int softCurrency,
            int hardCurrency
    ) {
    }
}
