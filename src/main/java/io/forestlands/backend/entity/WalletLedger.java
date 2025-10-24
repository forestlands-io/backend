package io.forestlands.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "wallet_ledger")
public class WalletLedger extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "delta_soft_currency", nullable = false)
    private int deltaSoftCurrency;

    @Column(name = "delta_hard_currency", nullable = false)
    private int deltaHardCurrency;

    @Column(name = "reason", nullable = false, length = 50)
    private String reason;

    @Column(name = "ref_type", nullable = false, length = 50)
    private String refType;

    @Column(name = "ref_id", nullable = false, length = 100)
    private String refId;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getDeltaSoftCurrency() {
        return deltaSoftCurrency;
    }

    public void setDeltaSoftCurrency(int deltaSoftCurrency) {
        this.deltaSoftCurrency = deltaSoftCurrency;
    }

    public int getDeltaHardCurrency() {
        return deltaHardCurrency;
    }

    public void setDeltaHardCurrency(int deltaHardCurrency) {
        this.deltaHardCurrency = deltaHardCurrency;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getRefType() {
        return refType;
    }

    public void setRefType(String refType) {
        this.refType = refType;
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }
}
