package io.forestlands.backend.utils;

import io.forestlands.backend.entity.Species;
import io.forestlands.backend.entity.UserSpeciesUnlock;
import io.forestlands.backend.entity.UserSpeciesUnlockMethod;
import io.forestlands.backend.entity.User;
import io.forestlands.backend.entity.CurrencyType;
import io.forestlands.backend.service.SpeciesService;
import io.forestlands.backend.service.UserSpeciesUnlockService;
import io.forestlands.backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@Profile("init-db")
public class DatabaseInitializer implements CommandLineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseInitializer.class);
    private static final String DEFAULT_USER_EMAIL = "user@mail.com";
    private static final String DEFAULT_USER_PASSWORD = "123";

    private final UserService userService;
    private final SpeciesService speciesService;
    private final UserSpeciesUnlockService userSpeciesUnlockService;

    public DatabaseInitializer(UserService userService,
                               SpeciesService speciesService,
                               UserSpeciesUnlockService userSpeciesUnlockService) {
        this.userService = userService;
        this.speciesService = speciesService;
        this.userSpeciesUnlockService = userSpeciesUnlockService;
    }

    @Override
    public void run(String... args) {
        LOGGER.info("Ensuring init-db seed data");

        User defaultUser = userService
                .findByEmail(DEFAULT_USER_EMAIL)
                .orElseGet(() -> {
                    LOGGER.info("Creating default user {}", DEFAULT_USER_EMAIL);
                    return userService.createUser(DEFAULT_USER_EMAIL, DEFAULT_USER_PASSWORD);
                });

        Species cherry = ensureSpecies("cherry", "Cherry", false, 100, 10);
        Species oak = ensureSpecies("oak", "Oak", false, 200, 20);
        ensureSpecies("rose", "Rose", true, 5, 30);

        ensureUnlock(defaultUser, cherry);
        ensureUnlock(defaultUser, oak);

        LOGGER.info("Done ensuring init-db seed data");
    }

    private Species ensureSpecies(String code, String name, boolean premium, int price, int sortOrder) {
        return speciesService.findByCode(code).orElseGet(() -> createSpecies(code, name, premium, price, sortOrder));
    }

    private Species createSpecies(String code, String name, boolean premium, int price, int sortOrder) {
        Species species = new Species();
        species.setCode(code);
        species.setName(name);
        species.setPremium(premium);
        species.setPrice(price);
        species.setSortOrder(sortOrder);
        return speciesService.save(species);
    }

    private void ensureUnlock(User user, Species species) {
        if (userSpeciesUnlockService.isSpeciesUnlocked(user, species)) {
            return;
        }

        UserSpeciesUnlock unlock = new UserSpeciesUnlock();
        unlock.setUser(user);
        unlock.setSpecies(species);
        unlock.setUnlockedAt(Instant.now());
        unlock.setMethod(UserSpeciesUnlockMethod.TEST_GRANT);
        unlock.setPricePaid(0);
        unlock.setCurrencyType(CurrencyType.NONE);
        unlock.setNotes("init-db seed");
        userSpeciesUnlockService.recordUnlock(unlock);
        LOGGER.info("Unlocked species {} for {}", species.getCode(), user.getEmail());
    }
}
