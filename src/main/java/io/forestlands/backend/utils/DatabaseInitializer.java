package io.forestlands.backend.utils;

import io.forestlands.backend.entity.Species;
import io.forestlands.backend.entity.User;
import io.forestlands.backend.service.SpeciesService;
import io.forestlands.backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("init-db")
public class DatabaseInitializer implements CommandLineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseInitializer.class);
    private final UserService userService;
    private final SpeciesService speciesService;

    public DatabaseInitializer(UserService userService, SpeciesService speciesService) {
        this.userService = userService;
        this.speciesService = speciesService;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userService.findByEmail("user@mail.com").isPresent()) {
            LOGGER.info("Database already initialized");
            return;
        }

        LOGGER.info("Initializing database");
        userService.createUser("user@mail.com", "123");
        createSpecies("cherry", "Cherry", false, 100, 10);
        createSpecies("oak", "Oak", false, 200, 20);
        createSpecies("rose", "Rose", true, 5, 30);
        LOGGER.info("Done initializing database");
        // System.exit(0);
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
}
