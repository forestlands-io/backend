package io.forestlands.backend.repository;

import io.forestlands.backend.entity.Species;
import io.forestlands.backend.entity.User;
import io.forestlands.backend.entity.UserSpeciesUnlock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserSpeciesUnlockRepository extends JpaRepository<UserSpeciesUnlock, Long> {

    boolean existsByUserAndSpecies(User user, Species species);

    List<UserSpeciesUnlock> findByUserOrderByUnlockedAtAsc(User user);
}
