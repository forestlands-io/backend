package io.forestlands.backend.controller;

import io.forestlands.backend.entity.FocusSession;
import io.forestlands.backend.entity.FocusSessionState;
import io.forestlands.backend.entity.Species;
import io.forestlands.backend.entity.TreeInventory;
import io.forestlands.backend.entity.User;
import io.forestlands.backend.entity.Wallet;
import io.forestlands.backend.service.FocusSessionService;
import io.forestlands.backend.service.TreeInventoryService;
import io.forestlands.backend.service.UserService;
import io.forestlands.backend.service.UserSpeciesUnlockService;
import io.forestlands.backend.service.WalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private FocusSessionService focusSessionService;

    @MockBean
    private TreeInventoryService treeInventoryService;

    @MockBean
    private WalletService walletService;

    @MockBean
    private UserSpeciesUnlockService userSpeciesUnlockService;

    @Test
    @WithMockUser(username = "user@example.com")
    void meReturnsSessionsInventoryAndWallet() throws Exception {
        User user = new User();
        user.setUuid(UUID.randomUUID());
        user.setEmail("user@example.com");
        when(userService.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        Species species = buildSpecies("oak", "Oak");

        FocusSession session = new FocusSession();
        session.setUuid(UUID.randomUUID());
        session.setState(FocusSessionState.SUCCESS);
        session.setSpecies(species);
        session.setPlannedMinutes(25);
        session.setDurationMinutes(25);
        session.setTag("work");
        session.setClientStartTime(Instant.parse("2026-01-01T10:00:00Z"));
        session.setClientEndTime(Instant.parse("2026-01-01T10:25:00Z"));
        session.setServerStartTime(Instant.parse("2026-01-01T10:00:01Z"));
        session.setServerEndTime(Instant.parse("2026-01-01T10:25:01Z"));
        when(focusSessionService.findRecentSessions(user)).thenReturn(List.of(session));

        TreeInventory tree = new TreeInventory();
        tree.setId(7L);
        tree.setUser(user);
        tree.setSpecies(species);
        tree.setPlaced(false);
        when(treeInventoryService.findInventory(user)).thenReturn(List.of(tree));

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setSoftCurrency(42);
        wallet.setHardCurrency(3);
        when(walletService.getOrCreate(user)).thenReturn(wallet);
        when(userSpeciesUnlockService.listUnlockedSpecies(user)).thenReturn(List.of(species));

        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessions[0].state").value("SUCCESS"))
                .andExpect(jsonPath("$.sessions[0].speciesCode").value("oak"))
                .andExpect(jsonPath("$.sessions[0].plannedMinutes").value(25))
                .andExpect(jsonPath("$.inventory[0].id").value(7))
                .andExpect(jsonPath("$.inventory[0].speciesCode").value("oak"))
                .andExpect(jsonPath("$.unlockedSpeciesCodes[0]").value("oak"))
                .andExpect(jsonPath("$.wallet.softCurrency").value(42))
                .andExpect(jsonPath("$.wallet.hardCurrency").value(3));
    }

    @Test
    void meResolvesUserFromJwtEmailClaim() throws Exception {
        User user = new User();
        user.setUuid(UUID.randomUUID());
        user.setEmail("user@mail.com");
        when(userService.findByEmail("user@mail.com")).thenReturn(Optional.of(user));
        when(focusSessionService.findRecentSessions(user)).thenReturn(List.of());
        when(treeInventoryService.findInventory(user)).thenReturn(List.of());

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setSoftCurrency(0);
        wallet.setHardCurrency(0);
        when(walletService.getOrCreate(user)).thenReturn(wallet);
        when(userSpeciesUnlockService.listUnlockedSpecies(user)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/users/me")
                        .with(jwt().jwt(jwt -> jwt.subject("936f44f7-ae6e-4802-949e-a461e65a05b7")
                                .claim("email", "user@mail.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessions").isArray())
                .andExpect(jsonPath("$.inventory").isArray())
                .andExpect(jsonPath("$.unlockedSpeciesCodes").isArray())
                .andExpect(jsonPath("$.wallet.softCurrency").value(0))
                .andExpect(jsonPath("$.wallet.hardCurrency").value(0));
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
