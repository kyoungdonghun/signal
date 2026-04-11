package com.kyoung.signal.controller;

import com.kyoung.signal.service.PipelineScheduler;
import com.kyoung.signal.service.PipelineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/pipeline")
public class PipelineController {

    private final PipelineService pipelineService;
    private final PipelineScheduler pipelineScheduler;

    public PipelineController(PipelineService pipelineService,
                              PipelineScheduler pipelineScheduler) {
        this.pipelineService = pipelineService;
        this.pipelineScheduler = pipelineScheduler;
    }

    // 단일 종목 수동 실행
    @PostMapping("/run")
    public ResponseEntity<PipelineService.PipelineResult> run(@RequestBody RunRequest request) {
        PipelineService.PipelineResult result = pipelineService.run(
                request.ticker(),
                request.rssFeedUrl(),
                request.rssFeedSource()
        );
        return ResponseEntity.ok(result);
    }

    // 스케줄러 전체 수동 트리거 (outcome 채움 + watchlist 전체 실행)
    @PostMapping("/scheduler/trigger")
    public ResponseEntity<Map<String, String>> triggerScheduler() {
        pipelineScheduler.runScheduled();
        return ResponseEntity.ok(Map.of("status", "triggered"));
    }

    public record RunRequest(String ticker, String rssFeedUrl, String rssFeedSource) {}
}
