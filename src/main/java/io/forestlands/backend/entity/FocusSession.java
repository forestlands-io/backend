package io.forestlands.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "focus_session")
public class FocusSession extends BaseEntity {

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "uuid", nullable = false, unique = true, updatable = false, length = 36)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "species_id", nullable = false)
    private Species species;

    @Column(name = "client_start_time", nullable = false)
    private Instant clientStartTime;

    @Column(name = "client_end_time")
    private Instant clientEndTime;

    @Column(name = "server_start_time", nullable = false)
    private Instant serverStartTime;

    @Column(name = "server_end_time")
    private Instant serverEndTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private FocusSessionState state = FocusSessionState.CREATED;

    @Column(name = "tag", length = 20)
    private String tag;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "planned_minutes")
    private Integer plannedMinutes;

    @Column(name = "flags_json", columnDefinition = "TEXT")
    private String flagsJson;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

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

    public Instant getClientStartTime() {
        return clientStartTime;
    }

    public void setClientStartTime(Instant clientStartTime) {
        this.clientStartTime = clientStartTime;
    }

    public Instant getClientEndTime() {
        return clientEndTime;
    }

    public void setClientEndTime(Instant clientEndTime) {
        this.clientEndTime = clientEndTime;
    }

    public Instant getServerStartTime() {
        return serverStartTime;
    }

    public void setServerStartTime(Instant serverStartTime) {
        this.serverStartTime = serverStartTime;
    }

    public Instant getServerEndTime() {
        return serverEndTime;
    }

    public void setServerEndTime(Instant serverEndTime) {
        this.serverEndTime = serverEndTime;
    }

    public FocusSessionState getState() {
        return state;
    }

    public void setState(FocusSessionState state) {
        this.state = state;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Integer getPlannedMinutes() {
        return plannedMinutes;
    }

    public void setPlannedMinutes(Integer plannedMinutes) {
        this.plannedMinutes = plannedMinutes;
    }

    public String getFlagsJson() {
        return flagsJson;
    }

    public void setFlagsJson(String flagsJson) {
        this.flagsJson = flagsJson;
    }
}
