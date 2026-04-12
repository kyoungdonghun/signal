package com.kyoung.signal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyoung.signal.domain.PipelineRunEntity;
import com.kyoung.signal.repository.OutcomeRecordRepository;
import com.kyoung.signal.repository.PipelineRunRepository;
import com.kyoung.signal.service.PipelineScheduler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/runs")
public class RunController {

    private final PipelineRunRepository pipelineRunRepository;
    private final OutcomeRecordRepository outcomeRecordRepository;
    private final PipelineScheduler.WatchlistProperties watchlistProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RunController(PipelineRunRepository pipelineRunRepository,
                         OutcomeRecordRepository outcomeRecordRepository,
                         PipelineScheduler.WatchlistProperties watchlistProperties) {
        this.pipelineRunRepository = pipelineRunRepository;
        this.outcomeRecordRepository = outcomeRecordRepository;
        this.watchlistProperties = watchlistProperties;
    }

    // ticker별 최신 run 목록 (브리핑 페이지) — 현재 watchlist 티커만 표시
    @GetMapping("/latest")
    public ResponseEntity<List<Map<String, Object>>> getLatest() {
        Set<String> activeTickers = watchlistProperties.getWatchlist().stream()
                .map(PipelineScheduler.WatchlistItem::getTicker)
                .collect(Collectors.toSet());

        List<PipelineRunEntity> runs = pipelineRunRepository.findLatestPerTicker()
                .stream()
                .filter(r -> activeTickers.contains(r.getTicker()))
                .toList();
        return ResponseEntity.ok(runs.stream().map(this::toSummary).toList());
    }

    // 전체 run 목록 (트랙레코드) — ticker 파라미터로 필터 가능
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @RequestParam(required = false) String ticker) {
        List<PipelineRunEntity> runs = ticker != null
                ? pipelineRunRepository.findByTickerOrderByExecutedAtDesc(ticker)
                : pipelineRunRepository.findTop50ByOrderByExecutedAtDesc();
        return ResponseEntity.ok(runs.stream().map(r -> {
            Map<String, Object> summary = toSummary(r);
            outcomeRecordRepository.findByRunId(r.getRunId()).ifPresent(o -> {
                summary.put("price1dAfter", o.getPrice1dAfter());
                summary.put("priceChangePct", o.getPriceChangePct());
            });
            return summary;
        }).toList());
    }

    // run 상세 (full_result 포함)
    @GetMapping("/{runId}")
    public ResponseEntity<Map<String, Object>> getDetail(@PathVariable String runId) {
        return pipelineRunRepository.findByRunId(runId)
                .map(entity -> {
                    Map<String, Object> detail = toSummary(entity);
                    try {
                        if (entity.getFullResult() != null) {
                            Object parsed = objectMapper.readValue(entity.getFullResult(), Object.class);
                            detail.put("fullResult", parsed);
                        }
                    } catch (Exception e) {
                        detail.put("fullResult", null);
                    }
                    outcomeRecordRepository.findByRunId(runId).ifPresent(o -> {
                        detail.put("price1dAfter", o.getPrice1dAfter());
                        detail.put("price1wAfter", o.getPrice1wAfter());
                        detail.put("priceChangePct", o.getPriceChangePct());
                    });
                    return ResponseEntity.ok(detail);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private Map<String, Object> toSummary(PipelineRunEntity r) {
        return new java.util.LinkedHashMap<>(Map.of(
                "runId", r.getRunId(),
                "ticker", r.getTicker(),
                "executedAt", r.getExecutedAt().toString() + "Z",
                "stability", r.getStability() != null ? r.getStability() : "",
                "crossResult", r.getCrossResult() != null ? r.getCrossResult() : "",
                "price", r.getPrice() != null ? r.getPrice() : 0.0,
                "rsi", r.getRsi() != null ? r.getRsi() : 0.0
        ));
    }
}
