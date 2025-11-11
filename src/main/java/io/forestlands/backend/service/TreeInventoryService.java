package io.forestlands.backend.service;

import io.forestlands.backend.entity.Species;
import io.forestlands.backend.entity.TreeInventory;
import io.forestlands.backend.entity.User;
import io.forestlands.backend.repository.TreeInventoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TreeInventoryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TreeInventoryService.class);

    private final TreeInventoryRepository treeInventoryRepository;

    public TreeInventoryService(TreeInventoryRepository treeInventoryRepository) {
        this.treeInventoryRepository = treeInventoryRepository;
    }

    @Transactional
    public TreeInventory addTreeToInventory(User user, Species species) {
        if (user == null) {
            throw new IllegalArgumentException("User is required to add tree to inventory");
        }
        if (species == null) {
            throw new IllegalArgumentException("Species is required to add tree to inventory");
        }
        TreeInventory treeInventory = new TreeInventory();
        treeInventory.setUser(user);
        treeInventory.setSpecies(species);
        treeInventory.setPlaced(false);
        treeInventory.setCellX(null);
        treeInventory.setCellY(null);
        TreeInventory saved = treeInventoryRepository.save(treeInventory);

        LOGGER.info(
                "Tree added to inventory userUuid={} speciesCode={} treeId={}",
                user.getUuid(),
                species.getCode(),
                saved.getId()
        );
        return saved;
    }

    @Transactional(readOnly = true)
    public List<TreeInventory> findInventory(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User is required to fetch inventory");
        }
        return treeInventoryRepository.findByUser(user);
    }

    @Transactional
    public TreeInventory placeTree(Long treeId, User user, int cellX, int cellY) {
        if (treeId == null) {
            throw new IllegalArgumentException("treeId is required");
        }
        if (user == null) {
            throw new IllegalArgumentException("User is required");
        }

        TreeInventory treeInventory = treeInventoryRepository
                .findByIdAndUser(treeId, user)
                .orElseThrow(() -> new IllegalArgumentException("Tree not found for user"));

        if (treeInventory.isPlaced()) {
            throw new IllegalStateException("Tree is already placed and cannot be moved");
        }

        treeInventory.setPlaced(true);
        treeInventory.setCellX(cellX);
        treeInventory.setCellY(cellY);

        TreeInventory saved = treeInventoryRepository.save(treeInventory);

        LOGGER.info(
                "Tree placed on map userUuid={} speciesCode={} treeId={} cell=({}, {})",
                user.getUuid(),
                saved.getSpecies().getCode(),
                saved.getId(),
                cellX,
                cellY
        );
        return saved;
    }
}
