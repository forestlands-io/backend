package io.forestlands.backend.repository;

import io.forestlands.backend.entity.Species;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpeciesRepository extends JpaRepository<Species, Long> {

    Optional<Species> findByCode(String speciesCode);

    List<Species> findAllByEnabledTrueOrderByNameAsc();
}
