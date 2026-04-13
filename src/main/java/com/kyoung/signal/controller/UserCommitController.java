package com.kyoung.signal.controller;

import com.kyoung.signal.domain.UserCommitEntity;
import com.kyoung.signal.repository.UserCommitRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user-commits")
public class UserCommitController {

    private final UserCommitRepository userCommitRepository;

    public UserCommitController(UserCommitRepository userCommitRepository) {
        this.userCommitRepository = userCommitRepository;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> save(@RequestBody CommitRequest request) {
        UserCommitEntity entity = userCommitRepository.findFirstByRunIdOrderByCommittedAtDesc(request.runId())
                .map(existing -> {
                    existing.update(
                            request.userCrossResult(),
                            request.userNote(),
                            request.agreedWithAi(),
                            request.userLevelView()
                    );
                    return existing;
                })
                .orElseGet(() -> UserCommitEntity.of(
                        request.runId(),
                        request.ticker(),
                        request.userCrossResult(),
                        request.userNote(),
                        request.agreedWithAi(),
                        request.userLevelView()
                ));
        userCommitRepository.save(entity);
        return ResponseEntity.ok(Map.of(
                "id", entity.getId() != null ? entity.getId() : 0,
                "runId", request.runId(),
                "status", "saved",
                "committedAt", entity.getCommittedAt(),
                "updatedAt", entity.getUpdatedAt()
        ));
    }

    @GetMapping("/{runId}")
    public ResponseEntity<?> getByRunId(@PathVariable String runId) {
        return userCommitRepository.findFirstByRunIdOrderByCommittedAtDesc(runId)
                .<ResponseEntity<?>>map(entity -> ResponseEntity.ok(toMap(entity)))
                .orElse(ResponseEntity.ok().body(null));
    }

    // 전체 commit 목록 (트랙레코드 비교뷰 용)
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAll() {
        return ResponseEntity.ok(
                userCommitRepository.findAll().stream()
                        .map(this::toMap)
                        .toList()
        );
    }

    private Map<String, Object> toMap(UserCommitEntity e) {
        Map<String, Object> m = new java.util.LinkedHashMap<>();
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

    public record CommitRequest(
            String runId,
            String ticker,
            String userCrossResult,
            String userNote,
            Boolean agreedWithAi,
            String userLevelView
    ) {}
}
