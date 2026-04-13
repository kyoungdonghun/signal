package com.kyoung.signal.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "level_commit_verdicts")
public class LevelCommitVerdictEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "run_id", nullable = false, length = 16)
    private String runId;

    @Column(name = "ticker", nullable = false, length = 20)
    private String ticker;

    @Column(name = "level_value", nullable = false)
    private Double levelValue;

    @Column(name = "level_type", nullable = false, length = 20)
    private String levelType;

    @Column(name = "basis", length = 40)
    private String basis;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "horizon", nullable = false, length = 10)
    private String horizon;

    @Column(name = "verdict", nullable = false, length = 20)
    private String verdict;

    @Column(name = "observed_price")
    private Double observedPrice;

    @Column(name = "checked_at", nullable = false)
    private LocalDateTime checkedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    protected LevelCommitVerdictEntity() {}

    public static LevelCommitVerdictEntity of(String runId, String ticker,
                                              Double levelValue, String levelType, String basis,
                                              String description, String horizon, String verdict,
                                              Double observedPrice, String notes) {
        LevelCommitVerdictEntity entity = new LevelCommitVerdictEntity();
        entity.runId = runId;
        entity.ticker = ticker;
        entity.levelValue = levelValue;
        entity.levelType = levelType;
        entity.basis = basis;
        entity.description = description;
        entity.horizon = horizon;
        entity.verdict = verdict;
        entity.observedPrice = observedPrice;
        entity.checkedAt = LocalDateTime.now();
        entity.notes = notes;
        return entity;
    }

    public void update(String verdict, Double observedPrice, String notes) {
        this.verdict = verdict;
        this.observedPrice = observedPrice;
        this.checkedAt = LocalDateTime.now();
        this.notes = notes;
    }

    public Long getId() { return id; }
    public String getRunId() { return runId; }
    public String getTicker() { return ticker; }
    public Double getLevelValue() { return levelValue; }
    public String getLevelType() { return levelType; }
    public String getBasis() { return basis; }
    public String getDescription() { return description; }
    public String getHorizon() { return horizon; }
    public String getVerdict() { return verdict; }
    public Double getObservedPrice() { return observedPrice; }
    public LocalDateTime getCheckedAt() { return checkedAt; }
    public String getNotes() { return notes; }
}
