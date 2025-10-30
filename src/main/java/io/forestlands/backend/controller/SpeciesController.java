package io.forestlands.backend.controller;

import io.forestlands.backend.controller.dto.SpeciesListResponse;
import io.forestlands.backend.controller.dto.SpeciesListResponse.SpeciesItem;
import io.forestlands.backend.entity.Species;
import io.forestlands.backend.service.SpeciesService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/species")
public class SpeciesController {

    private final SpeciesService speciesService;

    public SpeciesController(SpeciesService speciesService) {
        this.speciesService = speciesService;
    }

    @GetMapping
    public SpeciesListResponse listSpecies(
            @RequestParam(name = "includeDisabled", defaultValue = "false") boolean includeDisabled
    ) {
        List<Species> species = speciesService.listSpecies(includeDisabled);
        List<SpeciesItem> items = species.stream()
                .map(this::mapToItem)
                .toList();
        return new SpeciesListResponse(items);
    }

    private SpeciesItem mapToItem(Species species) {
        return new SpeciesItem(
                species.getUuid(),
                species.getCode(),
                species.getName(),
                species.isPremium(),
                species.getPrice(),
                species.isEnabled(),
                species.getSortOrder()
        );
    }
}
