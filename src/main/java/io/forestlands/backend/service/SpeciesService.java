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
        return speciesRepository.findAllByEnabledTrueOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public Optional<Species> findByUuid(UUID uuid) {
        return speciesRepository.findByUuid(uuid);
    }

    @Transactional
    public Species save(Species species) {
        if (species.getUuid() == null) {
            species.setUuid(UUID.randomUUID());
        }
        return speciesRepository.save(species);
    }
}
