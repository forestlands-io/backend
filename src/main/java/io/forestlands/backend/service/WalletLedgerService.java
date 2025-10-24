package io.forestlands.backend.service;

import io.forestlands.backend.entity.User;
import io.forestlands.backend.entity.WalletLedger;
import io.forestlands.backend.repository.WalletLedgerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WalletLedgerService {

    private final WalletLedgerRepository walletLedgerRepository;

    public WalletLedgerService(WalletLedgerRepository walletLedgerRepository) {
        this.walletLedgerRepository = walletLedgerRepository;
    }

    @Transactional
    public WalletLedger recordEntry(User user,
                                    int deltaSoftCurrency,
                                    int deltaHardCurrency,
                                    String reason,
                                    String refType,
                                    String refId) {
        WalletLedger ledger = new WalletLedger();
        ledger.setUser(user);
        ledger.setDeltaSoftCurrency(deltaSoftCurrency);
        ledger.setDeltaHardCurrency(deltaHardCurrency);
        ledger.setReason(reason);
        ledger.setRefType(refType);
        ledger.setRefId(refId);
        return walletLedgerRepository.save(ledger);
    }

    @Transactional(readOnly = true)
    public List<WalletLedger> findRecentEntries(User user) {
        return walletLedgerRepository.findByUserOrderByCreationDateDesc(user);
    }
}
