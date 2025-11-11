package io.forestlands.backend.controller;

import io.forestlands.backend.controller.dto.TreeInventoryListResponse;
import io.forestlands.backend.controller.dto.TreeInventoryListResponse.TreeInventoryItem;
import io.forestlands.backend.entity.TreeInventory;
import io.forestlands.backend.entity.User;
import io.forestlands.backend.service.TreeInventoryService;
import io.forestlands.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/api/v1/inventory/trees")
public class TreeInventoryController {

    private final TreeInventoryService treeInventoryService;
    private final UserService userService;

    public TreeInventoryController(TreeInventoryService treeInventoryService, UserService userService) {
        this.treeInventoryService = treeInventoryService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<TreeInventoryListResponse> listInventory(Authentication authentication) {
        User user = resolveUser(authentication);
        List<TreeInventory> items = treeInventoryService.findInventory(user);
        TreeInventoryListResponse response = new TreeInventoryListResponse(
                items.stream()
                        .map(this::mapItem)
                        .toList()
        );
        return ResponseEntity.ok(response);
    }

    private TreeInventoryItem mapItem(TreeInventory treeInventory) {
        return new TreeInventoryItem(
                treeInventory.getId(),
                treeInventory.getSpecies().getUuid(),
                treeInventory.getSpecies().getCode(),
                treeInventory.getSpecies().getName(),
                treeInventory.isPlaced(),
                treeInventory.getCellX(),
                treeInventory.getCellY()
        );
    }

    private User resolveUser(Authentication authentication) {
        String email = authentication.getName();
        return userService
                .findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "User not found"));
    }
}
