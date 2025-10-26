package io.forestlands.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(
        name = "user_species_unlocks",
        uniqueConstraints = @UniqueConstraint(name = "uq_user_species_unlock", columnNames = {"user_id", "species_id"}),
        indexes = {
                @Index(name = "idx_user_species_unlock_user", columnList = "user_id"),
                @Index(name = "idx_user_species_unlock_species", columnList = "species_id")
        }
)
public class UserSpeciesUnlock extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "species_id", nullable = false)
    private Species species;

    @Column(name = "unlocked_at", nullable = false)
    private Instant unlockedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, length = 20)
    private UserSpeciesUnlockMethod method;

    @Column(name = "price_paid", nullable = false)
    private int pricePaid;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency_type", nullable = false, length = 10)
    private CurrencyType currencyType;

    @Column(name = "notes", length = 255)
    private String notes;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Species getSpecies() {
        return species;
    }

    public void setSpecies(Species species) {
        this.species = species;
    }

    public Instant getUnlockedAt() {
        return unlockedAt;
    }

    public void setUnlockedAt(Instant unlockedAt) {
        this.unlockedAt = unlockedAt;
    }

    public UserSpeciesUnlockMethod getMethod() {
        return method;
    }

    public void setMethod(UserSpeciesUnlockMethod method) {
        this.method = method;
    }

    public int getPricePaid() {
        return pricePaid;
    }

    public void setPricePaid(int pricePaid) {
        this.pricePaid = pricePaid;
    }

    public CurrencyType getCurrencyType() {
        return currencyType;
    }

    public void setCurrencyType(CurrencyType currencyType) {
        this.currencyType = currencyType;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
