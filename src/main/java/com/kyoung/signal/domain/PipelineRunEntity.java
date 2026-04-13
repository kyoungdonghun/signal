package com.kyoung.signal.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pipeline_runs")
public class PipelineRunEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "run_id", nullable = false, unique = true, length = 16)
    private String runId;

    @Column(name = "ticker", nullable = false, length = 20)
    private String ticker;

    @Column(name = "executed_at", nullable = false)
    private LocalDateTime executedAt;

    @Column(name = "stability", length = 10)
    private String stability;

    @Column(name = "tr_confidence", length = 10)
    private String trConfidence;

    @Column(name = "cross_result", length = 20)
    private String crossResult;

    @Column(name = "news_direction", length = 10)
    private String newsDirection;

    @Column(name = "ca_confidence", length = 10)
    private String caConfidence;

    @Column(name = "price")
    private Double price;

    @Column(name = "ma20")
    private Double ma20;

    @Column(name = "ma60")
    private Double ma60;

    @Column(name = "rsi")
    private Double rsi;

    @Column(name = "volume_ratio")
    private Double volumeRatio;

    @Column(name = "level_commits", columnDefinition = "JSON")
    private String levelCommits;

    @Column(name = "full_result", columnDefinition = "JSON")
    private String fullResult;

    protected PipelineRunEntity() {}

    public static PipelineRunEntity of(String runId, String ticker, LocalDateTime executedAt,
                                       String stability, String trConfidence,
                                       String crossResult, String newsDirection, String caConfidence,
                                       Double price, Double ma20, Double ma60,
                                       Double rsi, Double volumeRatio,
                                       String levelCommits, String fullResult) {
        PipelineRunEntity e = new PipelineRunEntity();
        e.runId = runId;
        e.ticker = ticker;
        e.executedAt = executedAt;
        e.stability = stability;
        e.trConfidence = trConfidence;
        e.crossResult = crossResult;
        e.newsDirection = newsDirection;
        e.caConfidence = caConfidence;
        e.price = price;
        e.ma20 = ma20;
        e.ma60 = ma60;
        e.rsi = rsi;
        e.volumeRatio = volumeRatio;
        e.levelCommits = levelCommits;
        e.fullResult = fullResult;
        return e;
    }

    public Long getId()                  { return id; }
    public String getRunId()             { return runId; }
    public String getTicker()            { return ticker; }
    public LocalDateTime getExecutedAt() { return executedAt; }
    public String getStability()         { return stability; }
    public String getTrConfidence()      { return trConfidence; }
    public String getCrossResult()       { return crossResult; }
    public String getNewsDirection()     { return newsDirection; }
    public String getCaConfidence()      { return caConfidence; }
    public Double getPrice()             { return price; }
    public Double getMa20()              { return ma20; }
    public Double getMa60()              { return ma60; }
    public Double getRsi()               { return rsi; }
    public Double getVolumeRatio()       { return volumeRatio; }
    public String getLevelCommits()      { return levelCommits; }
    public String getFullResult()        { return fullResult; }
}
