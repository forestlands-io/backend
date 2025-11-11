package io.forestlands.backend.controller.dto;

import java.util.List;
import java.util.UUID;

public record TreeInventoryListResponse(List<TreeInventoryItem> items) {

    public record TreeInventoryItem(
            Long id,
            UUID speciesUuid,
            String speciesCode,
            String speciesName,
            boolean placed,
            Integer cellX,
            Integer cellY
    ) {
    }
}
