package com.kyoung.signal.controller;

import com.kyoung.signal.domain.LevelCommitVerdictEntity;
import com.kyoung.signal.domain.OutcomeRecordEntity;
import com.kyoung.signal.domain.PipelineRunEntity;
import com.kyoung.signal.domain.UserCommitEntity;
import com.kyoung.signal.repository.OutcomeRecordRepository;
import com.kyoung.signal.repository.PipelineRunRepository;
import com.kyoung.signal.repository.UserCommitRepository;
import com.kyoung.signal.service.CalibrationService;
import com.kyoung.signal.service.LevelCommitVerdictService;
import com.kyoung.signal.service.PipelineScheduler;
import com.kyoung.signal.service.ReasoningQualityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/meta")
public class MetaLayerController {

    private final LevelCommitVerdictService levelCommitVerdictService;
    private final PipelineRunRepository pipelineRunRepository;
    private final OutcomeRecordRepository outcomeRecordRepository;
    private final UserCommitRepository userCommitRepository;
    private final PipelineScheduler.WatchlistProperties watchlistProperties;
    private final ReasoningQualityService reasoningQualityService;
    private final CalibrationService calibrationService;

    public MetaLayerController(LevelCommitVerdictService levelCommitVerdictService,
                               PipelineRunRepository pipelineRunRepository,
                               OutcomeRecordRepository outcomeRecordRepository,
                               UserCommitRepository userCommitRepository,
                               PipelineScheduler.WatchlistProperties watchlistProperties,
                               ReasoningQualityService reasoningQualityService,
                               CalibrationService calibrationService) {
        this.levelCommitVerdictService = levelCommitVerdictService;
        this.pipelineRunRepository = pipelineRunRepository;
        this.outcomeRecordRepository = outcomeRecordRepository;
        this.userCommitRepository = userCommitRepository;
        this.watchlistProperties = watchlistProperties;
        this.reasoningQualityService = reasoningQualityService;
        this.calibrationService = calibrationService;
    }

    @GetMapping("/level-verdicts/{runId}")
    public ResponseEntity<List<Map<String, Object>>> getLevelVerdicts(@PathVariable String runId) {
        return ResponseEntity.ok(
                levelCommitVerdictService.getByRunId(runId).stream()
                        .map(v -> {
                            Map<String, Object> m = new java.util.LinkedHashMap<>();
                            m.put("runId", v.getRunId());
                            m.put("ticker", v.getTicker());
                            m.put("levelValue", v.getLevelValue());
                            m.put("levelType", v.getLevelType());
                            m.put("basis", v.getBasis() != null ? v.getBasis() : "");
                            m.put("description", v.getDescription() != null ? v.getDescription() : "");
                            m.put("horizon", v.getHorizon());
                            m.put("verdict", v.getVerdict());
                            m.put("observedPrice", v.getObservedPrice());
                            m.put("checkedAt", v.getCheckedAt() != null ? v.getCheckedAt().toString() : null);
                            m.put("notes", v.getNotes() != null ? v.getNotes() : "");
                            return m;
                        })
                        .toList()
        );
    }

    @GetMapping("/track-record")
    public ResponseEntity<List<Map<String, Object>>> getTrackRecord() {
        List<String> activeTickers = watchlistProperties.getWatchlist().stream()
                .map(PipelineScheduler.WatchlistItem::getTicker)
                .toList();

        List<Map<String, Object>> rows = pipelineRunRepository.findTop50ByTickerInOrderByExecutedAtDesc(activeTickers).stream()
                .map(this::toTrackRecordRow)
                .toList();

        return ResponseEntity.ok(rows);
    }

    @GetMapping("/drift")
    public ResponseEntity<List<Map<String, Object>>> getDrift() {
        List<String> activeTickers = watchlistProperties.getWatchlist().stream()
                .map(PipelineScheduler.WatchlistItem::getTicker)
                .toList();

        List<Map<String, Object>> rows = activeTickers.stream()
                .map(this::toTickerDriftRow)
                .toList();

        return ResponseEntity.ok(rows);
    }

    @GetMapping("/calibration")
    public ResponseEntity<List<Map<String, Object>>> getCalibration() {
        List<String> activeTickers = watchlistProperties.getWatchlist().stream()
                .map(PipelineScheduler.WatchlistItem::getTicker)
                .toList();

        List<Map<String, Object>> rows = pipelineRunRepository.findTop50ByTickerInOrderByExecutedAtDesc(activeTickers).stream()
                .map(run -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("runId", run.getRunId());
                    row.put("ticker", run.getTicker());
                    row.put("executedAt", run.getExecutedAt().toString());

                    OutcomeRecordEntity outcome = outcomeRecordRepository.findByRunId(run.getRunId()).orElse(null);
                    UserCommitEntity userCommit = userCommitRepository.findFirstByRunIdOrderByCommittedAtDesc(run.getRunId()).orElse(null);
                    row.putAll(calibrationService.evaluate(outcome, userCommit, run.getCrossResult()));
                    return row;
                })
                .toList();

        return ResponseEntity.ok(rows);
    }

    @GetMapping("/reasoning-quality/{runId}")
    public ResponseEntity<List<Map<String, Object>>> getReasoningQuality(@PathVariable String runId) {
        return pipelineRunRepository.findByRunId(runId)
                .map(run -> ResponseEntity.ok(
                        reasoningQualityService.evaluate(run).stream()
                                .map(check -> {
                                    Map<String, Object> m = new LinkedHashMap<>();
                                    m.put("path", check.path());
                                    m.put("status", check.status());
                                    m.put("score", check.score());
                                    m.put("hasSpecificReference", check.hasSpecificReference());
                                    m.put("hasConnection", check.hasConnection());
                                    m.put("hasFalsifiability", check.hasFalsifiability());
                                    m.put("note", check.note());
                                    return m;
                                })
                                .toList()
                ))
                .orElse(ResponseEntity.notFound().build());
    }

    private Map<String, Object> toTrackRecordRow(PipelineRunEntity run) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("runId", run.getRunId());
        row.put("ticker", run.getTicker());
        row.put("executedAt", run.getExecutedAt().toString());
        row.put("stability", run.getStability() != null ? run.getStability() : "");
        row.put("crossResult", run.getCrossResult() != null ? run.getCrossResult() : "");
        row.put("newsDirection", run.getNewsDirection() != null ? run.getNewsDirection() : "");
        row.put("price", run.getPrice());
        row.put("rsi", run.getRsi());

        OutcomeRecordEntity outcome = outcomeRecordRepository.findByRunId(run.getRunId()).orElse(null);
        if (outcome != null) {
            row.put("price1dAfter", outcome.getPrice1dAfter());
            row.put("price1wAfter", outcome.getPrice1wAfter());
            row.put("priceChangePct", outcome.getPriceChangePct());
        }

        UserCommitEntity userCommit = userCommitRepository.findFirstByRunIdOrderByCommittedAtDesc(run.getRunId())
                .orElse(null);
        if (userCommit != null) {
            row.put("userCommit", toUserCommitMap(userCommit));
        }

        row.put("calibration", calibrationService.evaluate(outcome, userCommit, run.getCrossResult()));

        List<LevelCommitVerdictEntity> verdicts = levelCommitVerdictService.getByRunId(run.getRunId());
        row.put("levelVerdictCount", verdicts.size());
        row.put("heldCount", verdicts.stream().filter(v -> "held".equalsIgnoreCase(v.getVerdict())).count());
        row.put("brokenCount", verdicts.stream().filter(v -> "broken".equalsIgnoreCase(v.getVerdict())).count());

        List<ReasoningQualityService.ReasoningCheck> reasoningChecks = reasoningQualityService.evaluate(run);
        row.put("reasoningStrongCount", reasoningChecks.stream().filter(c -> "strong".equalsIgnoreCase(c.status())).count());
        row.put("reasoningWeakCount", reasoningChecks.stream().filter(c -> "weak".equalsIgnoreCase(c.status())).count());

        List<PipelineRunEntity> tickerRuns = pipelineRunRepository.findByTickerOrderByExecutedAtDesc(run.getTicker());
        int index = findRunIndex(tickerRuns, run.getRunId());
        if (index >= 0 && index + 1 < tickerRuns.size()) {
            PipelineRunEntity previous = tickerRuns.get(index + 1);
            boolean crossChanged = !safe(run.getCrossResult()).equals(safe(previous.getCrossResult()));
            boolean stabilityChanged = !safe(run.getStability()).equals(safe(previous.getStability()));
            String driftState = (!crossChanged && !stabilityChanged) ? "stable" : (crossChanged && stabilityChanged ? "high" : "medium");

            row.put("driftState", driftState);
            row.put("driftFromRunId", previous.getRunId());
            row.put("previousCrossResult", safe(previous.getCrossResult()));
            row.put("previousStability", safe(previous.getStability()));
            row.put("crossChanged", crossChanged);
            row.put("stabilityChanged", stabilityChanged);
        } else {
            row.put("driftState", "insufficient");
            row.put("driftFromRunId", null);
            row.put("previousCrossResult", "");
            row.put("previousStability", "");
            row.put("crossChanged", false);
            row.put("stabilityChanged", false);
        }

        return row;
    }

    private Map<String, Object> toTickerDriftRow(String ticker) {
        List<PipelineRunEntity> runs = pipelineRunRepository.findByTickerOrderByExecutedAtDesc(ticker);
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("ticker", ticker);

        if (runs.size() < 2) {
            row.put("status", "insufficient");
            row.put("recentRunId", runs.isEmpty() ? null : runs.get(0).getRunId());
            row.put("previousRunId", null);
            row.put("crossChanged", false);
            row.put("stabilityChanged", false);
            row.put("driftState", "insufficient");
            return row;
        }

        PipelineRunEntity current = runs.get(0);
        PipelineRunEntity previous = runs.get(1);
        boolean crossChanged = !safe(current.getCrossResult()).equals(safe(previous.getCrossResult()));
        boolean stabilityChanged = !safe(current.getStability()).equals(safe(previous.getStability()));
        String driftState = (!crossChanged && !stabilityChanged) ? "stable" : (crossChanged && stabilityChanged ? "high" : "medium");

        row.put("status", "ok");
        row.put("recentRunId", current.getRunId());
        row.put("previousRunId", previous.getRunId());
        row.put("currentCrossResult", safe(current.getCrossResult()));
        row.put("previousCrossResult", safe(previous.getCrossResult()));
        row.put("currentStability", safe(current.getStability()));
        row.put("previousStability", safe(previous.getStability()));
        row.put("crossChanged", crossChanged);
        row.put("stabilityChanged", stabilityChanged);
        row.put("driftState", driftState);
        return row;
    }

    private Map<String, Object> toUserCommitMap(UserCommitEntity e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("runId", e.getRunId());
        m.put("ticker", e.getTicker());
        m.put("userCrossResult", e.getUserCrossResult());
        m.put("userNote", e.getUserNote());
        m.put("userLevelView", e.getUserLevelView());
        m.put("agreedWithAi", e.getAgreedWithAi());
        m.put("committedAt", e.getCommittedAt());
        m.put("updatedAt", e.getUpdatedAt());
        return m;
    }

    private int findRunIndex(List<PipelineRunEntity> runs, String runId) {
        for (int i = 0; i < runs.size(); i++) {
            if (runs.get(i).getRunId().equals(runId)) {
                return i;
            }
        }
        return -1;
    }

    private String safe(String value) {
        return value != null ? value : "";
    }
}
