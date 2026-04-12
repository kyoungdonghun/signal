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
        UserCommitEntity entity = UserCommitEntity.of(
                request.runId(),
                request.ticker(),
                request.userCrossResult(),
                request.userNote(),
                request.agreedWithAi(),
                request.userLevelView()
        );
        userCommitRepository.save(entity);
        return ResponseEntity.ok(Map.of(
                "id", entity.getId() != null ? entity.getId() : 0,
                "runId", request.runId(),
                "status", "saved"
        ));
    }

    @GetMapping("/{runId}")
    public ResponseEntity<List<UserCommitEntity>> getByRunId(@PathVariable String runId) {
        return ResponseEntity.ok(userCommitRepository.findByRunId(runId));
    }

    // 전체 commit 목록 (트랙레코드 비교뷰 용)
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAll() {
        return ResponseEntity.ok(
                userCommitRepository.findAll().stream()
                        .map(e -> {
                            Map<String, Object> m = new java.util.LinkedHashMap<>();
                            m.put("runId", e.getRunId());
                            m.put("ticker", e.getTicker());
                            m.put("userCrossResult", e.getUserCrossResult());
                            m.put("userNote", e.getUserNote());
                            m.put("userLevelView", e.getUserLevelView());
                            m.put("agreedWithAi", e.getAgreedWithAi());
                            m.put("committedAt", e.getCommittedAt() != null ? e.getCommittedAt() + "Z" : null);
                            return m;
                        })
                        .toList()
        );
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
