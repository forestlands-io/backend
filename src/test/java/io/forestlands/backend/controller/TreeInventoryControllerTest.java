package io.forestlands.backend.controller;

import io.forestlands.backend.entity.Species;
import io.forestlands.backend.entity.TreeInventory;
import io.forestlands.backend.entity.User;
import io.forestlands.backend.service.TreeInventoryService;
import io.forestlands.backend.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TreeInventoryController.class)
class TreeInventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TreeInventoryService treeInventoryService;

    @MockBean
    private UserService userService;

    @Test
    @WithMockUser(username = "user@example.com")
    void listInventoryReturnsMappedItems() throws Exception {
        User user = new User();
        user.setUuid(UUID.randomUUID());
        when(userService.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        TreeInventory treeInventory = new TreeInventory();
        treeInventory.setId(5L);
        treeInventory.setUser(user);
        Species species = buildSpecies("oak", "Oak");
        treeInventory.setSpecies(species);
        treeInventory.setPlaced(true);
        treeInventory.setCellX(3);
        treeInventory.setCellY(7);

        when(treeInventoryService.findInventory(user)).thenReturn(List.of(treeInventory));

        mockMvc.perform(get("/api/v1/inventory/trees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(5))
                .andExpect(jsonPath("$.items[0].speciesCode").value("oak"))
                .andExpect(jsonPath("$.items[0].speciesName").value("Oak"))
                .andExpect(jsonPath("$.items[0].placed").value(true))
                .andExpect(jsonPath("$.items[0].cellX").value(3))
                .andExpect(jsonPath("$.items[0].cellY").value(7));
    }

    private Species buildSpecies(String code, String name) {
        Species species = new Species();
        species.setUuid(UUID.randomUUID());
        species.setCode(code);
        species.setName(name);
        species.setPremium(false);
        species.setPrice(0);
        species.setEnabled(true);
        return species;
    }
}
