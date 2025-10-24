package io.forestlands.backend.repository;

import io.forestlands.backend.entity.FocusSession;
import io.forestlands.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FocusSessionRepository extends JpaRepository<FocusSession, Long> {

    Optional<FocusSession> findByUuid(UUID uuid);

    Optional<FocusSession> findByUuidAndUser(UUID uuid, User user);

    List<FocusSession> findByUserOrderByCreationDateDesc(User user);
}
