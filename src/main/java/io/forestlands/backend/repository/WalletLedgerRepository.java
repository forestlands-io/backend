package io.forestlands.backend.repository;

import io.forestlands.backend.entity.User;
import io.forestlands.backend.entity.WalletLedger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletLedgerRepository extends JpaRepository<WalletLedger, Long> {

    List<WalletLedger> findByUserOrderByCreationDateDesc(User user);
}
