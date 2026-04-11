package com.kyoung.signal.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "outcome_records")
public class OutcomeRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "run_id", nullable = false, length = 16)
    private String runId;

    @Column(name = "ticker", nullable = false, length = 20)
    private String ticker;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Column(name = "price_1d_after")
    private Double price1dAfter;

    @Column(name = "price_1w_after")
    private Double price1wAfter;

    @Column(name = "price_change_pct")
    private Double priceChangePct;

    protected OutcomeRecordEntity() {}

    public static OutcomeRecordEntity of(String runId, String ticker,
                                         Double price1dAfter, Double price1wAfter,
                                         Double priceChangePct) {
        OutcomeRecordEntity e = new OutcomeRecordEntity();
        e.runId = runId;
        e.ticker = ticker;
        e.recordedAt = LocalDateTime.now();
        e.price1dAfter = price1dAfter;
        e.price1wAfter = price1wAfter;
        e.priceChangePct = priceChangePct;
        return e;
    }

    public Long getId()              { return id; }
    public String getRunId()         { return runId; }
    public Double getPrice1wAfter()  { return price1wAfter; }
    public void setPrice1wAfter(Double v) { this.price1wAfter = v; }
}
