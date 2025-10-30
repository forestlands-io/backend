package io.forestlands.backend.controller.dto;

import java.util.List;
import java.util.UUID;

public record SpeciesListResponse(List<SpeciesItem> items) {

    public record SpeciesItem(
            UUID uuid,
            String code,
            String name,
            boolean isPremium,
            int price,
            boolean isEnabled,
            int sortOrder
    ) {
    }
}
