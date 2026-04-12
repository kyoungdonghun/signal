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

    public record CommitRequest(
            String runId,
            String ticker,
            String userCrossResult,
            String userNote,
            Boolean agreedWithAi,
            String userLevelView
    ) {}
}
