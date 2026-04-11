package com.kyoung.signal.controller;

import com.kyoung.signal.service.PipelineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pipeline")
public class PipelineController {

    private final PipelineService pipelineService;

    public PipelineController(PipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    @PostMapping("/run")
    public ResponseEntity<PipelineService.PipelineResult> run(@RequestBody RunRequest request) {
        PipelineService.PipelineResult result = pipelineService.run(
                request.ticker(),
                request.rssFeedUrl(),
                request.rssFeedSource()
        );
        return ResponseEntity.ok(result);
    }

    public record RunRequest(String ticker, String rssFeedUrl, String rssFeedSource) {}
}
