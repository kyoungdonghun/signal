package com.kyoung.signal.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyoung.signal.domain.LevelCommitVerdictEntity;
import com.kyoung.signal.domain.OutcomeRecordEntity;
import com.kyoung.signal.domain.PipelineRunEntity;
import com.kyoung.signal.repository.LevelCommitVerdictRepository;
import com.kyoung.signal.repository.OutcomeRecordRepository;
import com.kyoung.signal.repository.PipelineRunRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LevelCommitVerdictService {

    private final PipelineRunRepository pipelineRunRepository;
    private final OutcomeRecordRepository outcomeRecordRepository;
    private final LevelCommitVerdictRepository levelCommitVerdictRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LevelCommitVerdictService(PipelineRunRepository pipelineRunRepository,
                                     OutcomeRecordRepository outcomeRecordRepository,
                                     LevelCommitVerdictRepository levelCommitVerdictRepository) {
        this.pipelineRunRepository = pipelineRunRepository;
        this.outcomeRecordRepository = outcomeRecordRepository;
        this.levelCommitVerdictRepository = levelCommitVerdictRepository;
    }

    public void evaluatePendingVerdicts() {
        List<PipelineRunEntity> runs = pipelineRunRepository.findByLevelCommitsIsNotNull();
        for (PipelineRunEntity run : runs) {
            if (run.getLevelCommits() == null || run.getLevelCommits().isBlank() || "[]".equals(run.getLevelCommits())) {
                continue;
            }

            Optional<OutcomeRecordEntity> outcomeOpt = outcomeRecordRepository.findByRunId(run.getRunId());
            if (outcomeOpt.isEmpty()) {
                continue;
            }

            OutcomeRecordEntity outcome = outcomeOpt.get();
            List<RawLevelCommit> levelCommits = parseLevelCommits(run.getLevelCommits());
            if (levelCommits.isEmpty()) {
                continue;
            }

            if (outcome.getPrice1dAfter() != null) {
                for (RawLevelCommit levelCommit : levelCommits) {
                    upsertVerdict(run, levelCommit, "1d", outcome.getPrice1dAfter());
                }
            }

            if (outcome.getPrice1wAfter() != null) {
                for (RawLevelCommit levelCommit : levelCommits) {
                    upsertVerdict(run, levelCommit, "1w", outcome.getPrice1wAfter());
                }
            }
        }
    }

    public List<LevelCommitVerdictEntity> getByRunId(String runId) {
        return levelCommitVerdictRepository.findByRunIdOrderByHorizonAscLevelValueAsc(runId);
    }

    private List<RawLevelCommit> parseLevelCommits(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            System.err.println("[LevelCommitVerdictService] level_commit parse 실패: " + e.getMessage());
            return List.of();
        }
    }

    private void upsertVerdict(PipelineRunEntity run, RawLevelCommit raw, String horizon, Double observedPrice) {
        String normalizedType = raw.type() != null ? raw.type().trim().toLowerCase() : "";
        String normalizedBasis = raw.basis() != null ? raw.basis().trim() : "";

        VerdictResult verdict = evaluate(raw.level(), normalizedType, observedPrice);

        LevelCommitVerdictEntity entity = levelCommitVerdictRepository
                .findFirstByRunIdAndLevelValueAndLevelTypeAndBasisAndHorizon(
                        run.getRunId(),
                        raw.level(),
                        normalizedType,
                        normalizedBasis,
                        horizon
                )
                .map(existing -> {
                    existing.update(verdict.verdict(), observedPrice, verdict.notes());
                    return existing;
                })
                .orElseGet(() -> LevelCommitVerdictEntity.of(
                        run.getRunId(),
                        run.getTicker(),
                        raw.level(),
                        normalizedType,
                        normalizedBasis,
                        raw.description(),
                        horizon,
                        verdict.verdict(),
                        observedPrice,
                        verdict.notes()
                ));

        levelCommitVerdictRepository.save(entity);
    }

    private VerdictResult evaluate(double level, String type, Double observedPrice) {
        if (observedPrice == null) {
            return new VerdictResult("untested", "Observed price not available yet.");
        }

        return switch (type) {
            case "support" -> observedPrice >= level
                    ? new VerdictResult("held", "Observed price stayed at or above the committed support level.")
                    : new VerdictResult("broken", "Observed price fell below the committed support level.");
            case "resistance" -> observedPrice <= level
                    ? new VerdictResult("held", "Observed price stayed at or below the committed resistance level.")
                    : new VerdictResult("broken", "Observed price moved above the committed resistance level.");
            default -> new VerdictResult("ambiguous", "Unknown level type; automatic verdict could not be trusted.");
        };
    }

    private record RawLevelCommit(double level, String type, String basis, String description) {}
    private record VerdictResult(String verdict, String notes) {}
}
