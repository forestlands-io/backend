package io.forestlands.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.forestlands.backend.entity.FocusSession;
import io.forestlands.backend.entity.FocusSessionState;
import io.forestlands.backend.entity.Species;
import io.forestlands.backend.entity.User;
import io.forestlands.backend.entity.Wallet;
import io.forestlands.backend.repository.FocusSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FocusSessionService {

    private final FocusSessionRepository focusSessionRepository;
    private final SpeciesService speciesService;
    private final WalletService walletService;
    private final WalletLedgerService walletLedgerService;
    private final UserSpeciesUnlockService userSpeciesUnlockService;
    private final TreeInventoryService treeInventoryService;
    private final ObjectMapper objectMapper;

    public FocusSessionService(FocusSessionRepository focusSessionRepository,
                               SpeciesService speciesService,
                               WalletService walletService,
                               WalletLedgerService walletLedgerService,
                               UserSpeciesUnlockService userSpeciesUnlockService,
                               TreeInventoryService treeInventoryService,
                               ObjectMapper objectMapper) {
        this.focusSessionRepository = focusSessionRepository;
        this.speciesService = speciesService;
        this.walletService = walletService;
        this.walletLedgerService = walletLedgerService;
        this.userSpeciesUnlockService = userSpeciesUnlockService;
        this.treeInventoryService = treeInventoryService;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public Optional<FocusSession> findByUuid(UUID uuid) {
        return focusSessionRepository.findByUuid(uuid);
    }

    @Transactional(readOnly = true)
    public List<FocusSession> findRecentSessions(User user) {
        return focusSessionRepository.findByUserOrderByCreationDateDesc(user);
    }

    @Transactional
    public FocusSessionStartResult startSession(User user,
                                                UUID sessionUuid,
                                                String speciesCode,
                                                Instant clientStartTime,
                                                Integer plannedMinutes,
                                                String tag) {
        if (sessionUuid == null) {
            throw new IllegalArgumentException("sessionUuid is required");
        }
        if (clientStartTime == null) {
            throw new IllegalArgumentException("clientStartTime is required");
        }
        if (plannedMinutes != null && (plannedMinutes < 5 || plannedMinutes > 120)) {
            throw new IllegalArgumentException("plannedMinutes must be between 5 and 120");
        }

        Optional<FocusSession> existing = focusSessionRepository.findByUuidAndUser(sessionUuid, user);
        if (existing.isPresent()) {
            return new FocusSessionStartResult(existing.get(), false);
        }

        focusSessionRepository.findByUuid(sessionUuid).ifPresent(other -> {
            throw new IllegalArgumentException("Session UUID already in use");
        });

        Species species = null;
        if (speciesCode != null) {
            species = speciesService
                    .findByCode(speciesCode)
                    .orElseThrow(() -> new IllegalArgumentException("Species not found"));
            if (!userSpeciesUnlockService.isSpeciesUsable(user, species)) {
                throw new IllegalArgumentException("Species is not available to this user");
            }
        }

        FocusSession session = new FocusSession();
        session.setUuid(sessionUuid);
        session.setUser(user);
        session.setSpecies(species);
        session.setClientStartTime(clientStartTime);
        session.setServerStartTime(Instant.now());
        session.setTag(tag);
        session.setPlannedMinutes(plannedMinutes);
        session.setState(FocusSessionState.CREATED);

        FocusSession saved = focusSessionRepository.save(session);
        return new FocusSessionStartResult(saved, true);
    }

    @Transactional
    public FocusSessionCompletionResult completeSession(User user,
                                                        UUID sessionUuid,
                                                        Instant clientEndTime,
                                                        FocusSessionState newState) {
        if (sessionUuid == null) {
            throw new IllegalArgumentException("sessionUuid is required");
        }
        if (newState == null || newState == FocusSessionState.CREATED) {
            throw new IllegalArgumentException("Invalid session state");
        }

        FocusSession session = focusSessionRepository
                .findByUuidAndUser(sessionUuid, user)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        if (session.getState() != FocusSessionState.CREATED && session.getServerEndTime() != null) {
            Wallet wallet = walletService.getOrCreate(user);
            int validated = session.getDurationMinutes() == null ? 0 : session.getDurationMinutes();
            int awarded = session.getState() == FocusSessionState.SUCCESS ? validated : 0;
            return new FocusSessionCompletionResult(session, validated, awarded, wallet);
        }

        if (newState == FocusSessionState.SUCCESS && clientEndTime == null) {
            throw new IllegalArgumentException("clientEndTime is required for successful sessions");
        }

        Instant serverEndTime = Instant.now();
        session.setServerEndTime(serverEndTime);
        session.setClientEndTime(clientEndTime);
        session.setState(newState);

        long serverDurationMinutes = Duration.between(session.getServerStartTime(), serverEndTime).toMinutes();
        if (serverDurationMinutes < 0) {
            throw new IllegalStateException("Server end time precedes start time");
        }

        long clientDurationMinutes = clientEndTime != null
                ? Duration.between(session.getClientStartTime(), clientEndTime).toMinutes()
                : serverDurationMinutes;

        if (clientDurationMinutes < 0) {
            throw new IllegalArgumentException("Client end time precedes client start time");
        }

        int drift = Math.toIntExact(Math.abs(clientDurationMinutes - serverDurationMinutes));

        List<String> anomalies = new ArrayList<>();
        if (drift > 0) {
            anomalies.add("DRIFT_" + drift + "M");
        }

        if (newState == FocusSessionState.SUCCESS) {
            if (serverDurationMinutes < 5) {
                throw new IllegalArgumentException("Session shorter than minimum duration");
            }
            if (drift > 3) {
                throw new IllegalArgumentException("Session drift exceeds tolerance");
            }
            if (serverDurationMinutes > 120) {
                anomalies.add("SERVER_DURATION_CLAMPED");
            }
            int validatedMinutes = (int) Math.min(Math.max(serverDurationMinutes, 5), 120);
            session.setDurationMinutes(validatedMinutes);
        } else {
            session.setDurationMinutes((int) serverDurationMinutes);
        }

        session.setFlagsJson(writeFlags(anomalies));

        Wallet wallet = null;
        int softAwarded = 0;
        if (newState == FocusSessionState.SUCCESS) {
            int rewardMinutes = session.getDurationMinutes();
            softAwarded = rewardMinutes;
            wallet = walletService.adjustBalance(user, rewardMinutes, 0);
            walletLedgerService.recordEntry(
                    user,
                    rewardMinutes,
                    0,
                    "FOCUS_REWARD",
                    "FOCUS_SESSION",
                    session.getUuid().toString()
            );
            if (session.getSpecies() != null) {
                treeInventoryService.addTreeToInventory(user, session.getSpecies());
            }
        } else {
            wallet = walletService.getOrCreate(user);
        }

        FocusSession saved = focusSessionRepository.save(session);
        return new FocusSessionCompletionResult(
                saved,
                session.getDurationMinutes() == null ? 0 : session.getDurationMinutes(),
                softAwarded,
                wallet
        );
    }

    private String writeFlags(List<String> anomalies) {
        if (anomalies == null || anomalies.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(anomalies);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize anomalies", ex);
        }
    }

    public record FocusSessionStartResult(FocusSession session, boolean created) {
    }

    public record FocusSessionCompletionResult(
            FocusSession session,
            int validatedMinutes,
            int softCurrencyAwarded,
            Wallet wallet
    ) {
    }
}
