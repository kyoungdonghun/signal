package com.kyoung.signal.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_commits")
public class UserCommitEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "run_id", nullable = false, length = 16)
    private String runId;

    @Column(name = "ticker", nullable = false, length = 20)
    private String ticker;

    @Column(name = "committed_at", nullable = false)
    private LocalDateTime committedAt;

    @Column(name = "user_cross_result", length = 20)
    private String userCrossResult;

    @Column(name = "user_note", columnDefinition = "TEXT")
    private String userNote;

    @Column(name = "agreed_with_ai")
    private Boolean agreedWithAi;

    protected UserCommitEntity() {}

    public static UserCommitEntity of(String runId, String ticker,
                                      String userCrossResult, String userNote,
                                      Boolean agreedWithAi) {
        UserCommitEntity e = new UserCommitEntity();
        e.runId = runId;
        e.ticker = ticker;
        e.committedAt = LocalDateTime.now();
        e.userCrossResult = userCrossResult;
        e.userNote = userNote;
        e.agreedWithAi = agreedWithAi;
        return e;
    }

    public Long getId()    { return id; }
    public String getRunId() { return runId; }
}
