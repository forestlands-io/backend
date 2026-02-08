package io.forestlands.backend.utils;

import io.forestlands.backend.entity.Species;
import io.forestlands.backend.entity.User;
import io.forestlands.backend.entity.UserSpeciesUnlock;
import io.forestlands.backend.service.SpeciesService;
import io.forestlands.backend.service.UserService;
import io.forestlands.backend.service.UserSpeciesUnlockService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatabaseInitializerTest {

    @Mock
    private UserService userService;

    @Mock
    private SpeciesService speciesService;

    @Mock
    private UserSpeciesUnlockService userSpeciesUnlockService;

    @InjectMocks
    private DatabaseInitializer databaseInitializer;

    @Test
    void runCreatesMissingUserSpeciesAndUnlocks() {
        User user = buildUser("user@mail.com");
        Species cherry = buildSpecies("cherry", "Cherry");
        Species oak = buildSpecies("oak", "Oak");
        Species rose = buildSpecies("rose", "Rose");

        when(userService.findByEmail("user@mail.com")).thenReturn(Optional.empty());
        when(userService.createUser("user@mail.com", "123")).thenReturn(user);

        when(speciesService.findByCode("cherry")).thenReturn(Optional.empty());
        when(speciesService.findByCode("oak")).thenReturn(Optional.empty());
        when(speciesService.findByCode("rose")).thenReturn(Optional.empty());
        when(speciesService.save(any(Species.class))).thenReturn(cherry, oak, rose);

        when(userSpeciesUnlockService.isSpeciesUnlocked(user, cherry)).thenReturn(false);
        when(userSpeciesUnlockService.isSpeciesUnlocked(user, oak)).thenReturn(false);

        databaseInitializer.run();

        verify(userService, times(1)).createUser("user@mail.com", "123");
        verify(speciesService, times(3)).save(any(Species.class));
        verify(userSpeciesUnlockService, times(2)).recordUnlock(any(UserSpeciesUnlock.class));
    }

    @Test
    void runIsIdempotentAndOnlyCreatesMissingUnlocks() {
        User user = buildUser("user@mail.com");
        Species cherry = buildSpecies("cherry", "Cherry");
        Species oak = buildSpecies("oak", "Oak");
        Species rose = buildSpecies("rose", "Rose");

        when(userService.findByEmail("user@mail.com")).thenReturn(Optional.of(user));
        when(speciesService.findByCode("cherry")).thenReturn(Optional.of(cherry));
        when(speciesService.findByCode("oak")).thenReturn(Optional.of(oak));
        when(speciesService.findByCode("rose")).thenReturn(Optional.of(rose));

        when(userSpeciesUnlockService.isSpeciesUnlocked(user, cherry)).thenReturn(true);
        when(userSpeciesUnlockService.isSpeciesUnlocked(user, oak)).thenReturn(false);

        databaseInitializer.run();

        verify(userService, times(0)).createUser("user@mail.com", "123");
        verify(speciesService, times(0)).save(any(Species.class));

        ArgumentCaptor<UserSpeciesUnlock> unlockCaptor = ArgumentCaptor.forClass(UserSpeciesUnlock.class);
        verify(userSpeciesUnlockService, times(1)).recordUnlock(unlockCaptor.capture());
        UserSpeciesUnlock unlock = unlockCaptor.getValue();
        assertEquals(user, unlock.getUser());
        assertEquals(oak, unlock.getSpecies());
        assertEquals("init-db seed", unlock.getNotes());
        assertNotNull(unlock.getUnlockedAt());
    }

    private User buildUser(String email) {
        User user = new User();
        user.setUuid(UUID.randomUUID());
        user.setEmail(email);
        return user;
    }

    private Species buildSpecies(String code, String name) {
        Species species = new Species();
        species.setUuid(UUID.randomUUID());
        species.setCode(code);
        species.setName(name);
        species.setEnabled(true);
        species.setPremium(false);
        return species;
    }
}
