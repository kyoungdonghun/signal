package com.kyoung.signal.controller;

import com.kyoung.signal.domain.SchedulerLogEntity;
import com.kyoung.signal.repository.SchedulerLogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/scheduler-logs")
public class SchedulerLogController {

    private final SchedulerLogRepository schedulerLogRepository;

    public SchedulerLogController(SchedulerLogRepository schedulerLogRepository) {
        this.schedulerLogRepository = schedulerLogRepository;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getLogs() {
        List<Map<String, Object>> result = schedulerLogRepository.findTop20ByOrderByStartedAtDesc()
                .stream()
                .map(e -> {
                    Map<String, Object> m = new java.util.LinkedHashMap<>();
                    m.put("id", e.getId());
                    m.put("startedAt", e.getStartedAt().toString() + "Z");
                    m.put("completedAt", e.getCompletedAt() != null ? e.getCompletedAt().toString() + "Z" : null);
                    m.put("tickersAttempted", e.getTickersAttempted());
                    m.put("tickersSucceeded", e.getTickersSucceeded());
                    m.put("tickersFailed", e.getTickersFailed());
                    m.put("status", e.getStatus());
                    return m;
                })
                .toList();
        return ResponseEntity.ok(result);
    }
}
