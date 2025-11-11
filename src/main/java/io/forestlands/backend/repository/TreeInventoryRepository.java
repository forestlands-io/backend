package io.forestlands.backend.repository;

import io.forestlands.backend.entity.Species;
import io.forestlands.backend.entity.TreeInventory;
import io.forestlands.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TreeInventoryRepository extends JpaRepository<TreeInventory, Long> {
    List<TreeInventory> findByUser(User user);

    List<TreeInventory> findByUserAndPlaced(User user, boolean placed);

    long countByUserAndSpeciesAndPlacedFalse(User user, Species species);

    Optional<TreeInventory> findByIdAndUser(Long id, User user);
}
