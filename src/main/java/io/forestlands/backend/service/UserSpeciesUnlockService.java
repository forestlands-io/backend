package io.forestlands.backend.service;

import io.forestlands.backend.entity.Species;
import io.forestlands.backend.entity.User;
import io.forestlands.backend.entity.UserSpeciesUnlock;
import io.forestlands.backend.repository.UserSpeciesUnlockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserSpeciesUnlockService {

    private final UserSpeciesUnlockRepository userSpeciesUnlockRepository;

    public UserSpeciesUnlockService(UserSpeciesUnlockRepository userSpeciesUnlockRepository) {
        this.userSpeciesUnlockRepository = userSpeciesUnlockRepository;
    }

    @Transactional(readOnly = true)
    public boolean isSpeciesUnlocked(User user, Species species) {
        return userSpeciesUnlockRepository.existsByUserAndSpecies(user, species);
    }

    @Transactional(readOnly = true)
    public boolean isSpeciesUsable(User user, Species species) {
        if (species == null || !species.isEnabled()) {
            return false;
        }
        if (species.isDefaultAvailable()) {
            return true;
        }
        return isSpeciesUnlocked(user, species);
    }

    @Transactional(readOnly = true)
    public List<Species> listUnlockedSpecies(User user) {
        return userSpeciesUnlockRepository
                .findByUserOrderByUnlockedAtAsc(user)
                .stream()
                .map(UserSpeciesUnlock::getSpecies)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserSpeciesUnlock recordUnlock(UserSpeciesUnlock unlock) {
        if (unlock.getUnlockedAt() == null) {
            unlock.setUnlockedAt(Instant.now());
        }
        return userSpeciesUnlockRepository.save(unlock);
    }
}
