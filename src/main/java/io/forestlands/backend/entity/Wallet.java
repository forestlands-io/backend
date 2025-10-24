package io.forestlands.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "wallets")
public class Wallet extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "soft_currency", nullable = false)
    private int softCurrency = 0;

    @Column(name = "hard_currency", nullable = false)
    private int hardCurrency = 0;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getSoftCurrency() {
        return softCurrency;
    }

    public void setSoftCurrency(int softCurrency) {
        this.softCurrency = softCurrency;
    }

    public int getHardCurrency() {
        return hardCurrency;
    }

    public void setHardCurrency(int hardCurrency) {
        this.hardCurrency = hardCurrency;
    }
}
