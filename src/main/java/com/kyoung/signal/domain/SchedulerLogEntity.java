package com.kyoung.signal.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "scheduler_logs")
public class SchedulerLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "tickers_attempted", length = 200)
    private String tickersAttempted;

    @Column(name = "tickers_succeeded", length = 200)
    private String tickersSucceeded;

    @Column(name = "tickers_failed", length = 200)
    private String tickersFailed;

    @Column(name = "status", length = 20)
    private String status; // RUNNING, SUCCESS, PARTIAL, FAILED

    protected SchedulerLogEntity() {}

    public static SchedulerLogEntity start(LocalDateTime startedAt, String tickersAttempted) {
        SchedulerLogEntity e = new SchedulerLogEntity();
        e.startedAt = startedAt;
        e.tickersAttempted = tickersAttempted;
        e.status = "RUNNING";
        return e;
    }

    public void complete(LocalDateTime completedAt, String succeeded, String failed) {
        this.completedAt = completedAt;
        this.tickersSucceeded = succeeded;
        this.tickersFailed = failed;
        this.status = failed == null || failed.isBlank() ? "SUCCESS"
                : succeeded == null || succeeded.isBlank() ? "FAILED"
                : "PARTIAL";
    }

    public Long getId()                   { return id; }
    public LocalDateTime getStartedAt()   { return startedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public String getTickersAttempted()   { return tickersAttempted; }
    public String getTickersSucceeded()   { return tickersSucceeded; }
    public String getTickersFailed()      { return tickersFailed; }
    public String getStatus()             { return status; }
}
