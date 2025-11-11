package io.forestlands.backend.service;

import io.forestlands.backend.entity.Species;
import io.forestlands.backend.entity.TreeInventory;
import io.forestlands.backend.entity.User;
import io.forestlands.backend.repository.TreeInventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TreeInventoryServiceTest {

    @Mock
    private TreeInventoryRepository treeInventoryRepository;

    private TreeInventoryService treeInventoryService;

    @BeforeEach
    void setUp() {
        treeInventoryService = new TreeInventoryService(treeInventoryRepository);
    }

    @Test
    void addTreeToInventoryCreatesUnplacedEntry() {
        User user = buildUser();
        Species species = buildSpecies("oak");
        when(treeInventoryRepository.save(any(TreeInventory.class))).thenAnswer(invocation -> {
            TreeInventory tree = invocation.getArgument(0);
            tree.setId(10L);
            return tree;
        });

        TreeInventory saved = treeInventoryService.addTreeToInventory(user, species);

        assertNotNull(saved);
        assertEquals(10L, saved.getId());
        assertFalse(saved.isPlaced());
        assertNull(saved.getCellX());
        assertNull(saved.getCellY());

        ArgumentCaptor<TreeInventory> captor = ArgumentCaptor.forClass(TreeInventory.class);
        verify(treeInventoryRepository).save(captor.capture());
        TreeInventory persisted = captor.getValue();
        assertEquals(user, persisted.getUser());
        assertEquals(species, persisted.getSpecies());
    }

    @Test
    void placeTreeRejectsAlreadyPlacedEntry() {
        User user = buildUser();
        Species species = buildSpecies("pine");
        TreeInventory placed = new TreeInventory();
        placed.setId(99L);
        placed.setUser(user);
        placed.setSpecies(species);
        placed.setPlaced(true);

        when(treeInventoryRepository.findByIdAndUser(99L, user)).thenReturn(Optional.of(placed));

        assertThrows(IllegalStateException.class, () -> treeInventoryService.placeTree(99L, user, 5, 6));

        verify(treeInventoryRepository, never()).save(any(TreeInventory.class));
    }

    @Test
    void placeTreeUpdatesCoordinates() {
        User user = buildUser();
        Species species = buildSpecies("maple");
        TreeInventory tree = new TreeInventory();
        tree.setId(7L);
        tree.setUser(user);
        tree.setSpecies(species);
        tree.setPlaced(false);

        when(treeInventoryRepository.findByIdAndUser(7L, user)).thenReturn(Optional.of(tree));
        when(treeInventoryRepository.save(tree)).thenReturn(tree);

        TreeInventory placed = treeInventoryService.placeTree(7L, user, 3, 4);

        assertNotNull(placed);
        assertEquals(3, placed.getCellX());
        assertEquals(4, placed.getCellY());
        assertTrue(placed.isPlaced());
    }

    private User buildUser() {
        User user = new User();
        user.setUuid(UUID.randomUUID());
        return user;
    }

    private Species buildSpecies(String code) {
        Species species = new Species();
        species.setCode(code);
        species.setUuid(UUID.randomUUID());
        species.setName(code);
        species.setPremium(false);
        species.setPrice(0);
        species.setEnabled(true);
        return species;
    }
}
