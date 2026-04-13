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

    @Column(name = "user_level_view", columnDefinition = "TEXT")
    private String userLevelView;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected UserCommitEntity() {}

    public static UserCommitEntity of(String runId, String ticker,
                                      String userCrossResult, String userNote,
                                      Boolean agreedWithAi, String userLevelView) {
        UserCommitEntity e = new UserCommitEntity();
        e.runId = runId;
        e.ticker = ticker;
        e.committedAt = LocalDateTime.now();
        e.userCrossResult = userCrossResult;
        e.userNote = userNote;
        e.agreedWithAi = agreedWithAi;
        e.userLevelView = userLevelView;
        e.updatedAt = e.committedAt;
        return e;
    }

    public void update(String userCrossResult, String userNote,
                       Boolean agreedWithAi, String userLevelView) {
        this.userCrossResult = userCrossResult;
        this.userNote = userNote;
        this.agreedWithAi = agreedWithAi;
        this.userLevelView = userLevelView;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId()                { return id; }
    public String getRunId()           { return runId; }
    public String getTicker()          { return ticker; }
    public String getCommittedAt()     { return committedAt != null ? committedAt.toString() : null; }
    public String getUserCrossResult() { return userCrossResult; }
    public String getUserNote()        { return userNote; }
    public Boolean getAgreedWithAi()   { return agreedWithAi; }
    public String getUserLevelView()   { return userLevelView; }
    public String getUpdatedAt()       { return updatedAt != null ? updatedAt.toString() : null; }
}
