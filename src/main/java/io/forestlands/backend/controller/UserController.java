package io.forestlands.backend.controller;

import io.forestlands.backend.controller.dto.UserMeResponse;
import io.forestlands.backend.entity.FocusSession;
import io.forestlands.backend.entity.TreeInventory;
import io.forestlands.backend.entity.User;
import io.forestlands.backend.entity.Wallet;
import io.forestlands.backend.service.FocusSessionService;
import io.forestlands.backend.service.TreeInventoryService;
import io.forestlands.backend.service.UserSpeciesUnlockService;
import io.forestlands.backend.service.UserService;
import io.forestlands.backend.service.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final FocusSessionService focusSessionService;
    private final TreeInventoryService treeInventoryService;
    private final WalletService walletService;
    private final UserSpeciesUnlockService userSpeciesUnlockService;

    public UserController(UserService userService,
                          FocusSessionService focusSessionService,
                          TreeInventoryService treeInventoryService,
                          WalletService walletService,
                          UserSpeciesUnlockService userSpeciesUnlockService) {
        this.userService = userService;
        this.focusSessionService = focusSessionService;
        this.treeInventoryService = treeInventoryService;
        this.walletService = walletService;
        this.userSpeciesUnlockService = userSpeciesUnlockService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserMeResponse> me(Authentication authentication) {
        User user = resolveUser(authentication);

        List<UserMeResponse.SessionItem> sessions = focusSessionService.findRecentSessions(user)
                .stream()
                .map(this::mapSession)
                .toList();

        List<UserMeResponse.InventoryItem> inventory = treeInventoryService.findInventory(user)
                .stream()
                .map(this::mapInventory)
                .toList();

        List<String> unlockedSpeciesCodes = userSpeciesUnlockService.listUnlockedSpecies(user)
                .stream()
                .map(species -> species.getCode())
                .toList();

        Wallet wallet = walletService.getOrCreate(user);

        UserMeResponse response = new UserMeResponse(
                sessions,
                inventory,
                unlockedSpeciesCodes,
                new UserMeResponse.WalletSummary(wallet.getSoftCurrency(), wallet.getHardCurrency())
        );
        return ResponseEntity.ok(response);
    }

    private UserMeResponse.SessionItem mapSession(FocusSession session) {
        String speciesCode = session.getSpecies() == null ? null : session.getSpecies().getCode();
        String speciesName = session.getSpecies() == null ? null : session.getSpecies().getName();
        return new UserMeResponse.SessionItem(
                session.getUuid(),
                session.getState(),
                speciesCode,
                speciesName,
                session.getPlannedMinutes(),
                session.getDurationMinutes(),
                session.getTag(),
                session.getClientStartTime(),
                session.getClientEndTime(),
                session.getServerStartTime(),
                session.getServerEndTime()
        );
    }

    private UserMeResponse.InventoryItem mapInventory(TreeInventory item) {
        return new UserMeResponse.InventoryItem(
                item.getId(),
                item.getSpecies().getUuid(),
                item.getSpecies().getCode(),
                item.getSpecies().getName(),
                item.isPlaced(),
                item.getCellX(),
                item.getCellY()
        );
    }

    private User resolveUser(Authentication authentication) {
        String email = extractEmail(authentication);
        return userService
                .findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "User not found"));
    }

    private String extractEmail(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            String jwtEmail = jwt.getClaimAsString("email");
            if (jwtEmail != null && !jwtEmail.isBlank()) {
                return jwtEmail;
            }
        }
        return authentication.getName();
    }
}
