package io.forestlands.backend.service;

import io.forestlands.backend.entity.Species;
import io.forestlands.backend.repository.SpeciesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SpeciesService {

    private final SpeciesRepository speciesRepository;

    public SpeciesService(SpeciesRepository speciesRepository) {
        this.speciesRepository = speciesRepository;
    }

    @Transactional(readOnly = true)
    public List<Species> listEnabledSpecies() {
        return listSpecies(false);
    }

    @Transactional(readOnly = true)
    public List<Species> listSpecies(boolean includeDisabled) {
        if (includeDisabled) {
            return speciesRepository.findAllByOrderBySortOrderAscNameAsc();
        }
        return speciesRepository.findAllByEnabledTrueOrderBySortOrderAscNameAsc();
    }

    @Transactional(readOnly = true)
    public Optional<Species> findByCode(String speciesCode) {
        return speciesRepository.findByCode(speciesCode);
    }

    @Transactional
    public Species save(Species species) {
        if (species.getUuid() == null) {
            species.setUuid(UUID.randomUUID());
        }
        if (species.getCode() == null || species.getCode().isBlank()) {
            throw new IllegalArgumentException("Species code is required");
        }
        return speciesRepository.save(species);
    }
}
