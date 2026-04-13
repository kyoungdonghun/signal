package com.kyoung.signal.repository;

import com.kyoung.signal.domain.LevelCommitVerdictEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LevelCommitVerdictRepository extends JpaRepository<LevelCommitVerdictEntity, Long> {
    List<LevelCommitVerdictEntity> findByRunIdOrderByHorizonAscLevelValueAsc(String runId);

    Optional<LevelCommitVerdictEntity> findFirstByRunIdAndLevelValueAndLevelTypeAndBasisAndHorizon(
            String runId,
            Double levelValue,
            String levelType,
            String basis,
            String horizon
    );
}
