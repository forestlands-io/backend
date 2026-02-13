package io.forestlands.backend.service;

import io.forestlands.backend.entity.User;
import io.forestlands.backend.entity.Wallet;
import io.forestlands.backend.repository.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WalletService.class);
    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Transactional
    public Wallet getOrCreate(User user) {
        return walletRepository
                .findByUser(user)
                .orElseGet(() -> {
                    Wallet wallet = new Wallet();
                    wallet.setUser(user);
                    return walletRepository.save(wallet);
                });
    }

    @Transactional
    public Wallet adjustBalance(User user, int deltaSoftCurrency, int deltaHardCurrency) {
        Wallet wallet = getOrCreate(user);
        wallet.setSoftCurrency(wallet.getSoftCurrency() + deltaSoftCurrency);
        wallet.setHardCurrency(wallet.getHardCurrency() + deltaHardCurrency);
        LOGGER.info("Added {} soft currency and {} hard currency to wallet of user {}", deltaSoftCurrency, deltaHardCurrency, user.getUuid());
        return walletRepository.save(wallet);
    }
}
