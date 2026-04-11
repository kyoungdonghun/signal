package com.kyoung.signal.repository;

import com.kyoung.signal.domain.PipelineRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PipelineRunRepository extends JpaRepository<PipelineRunEntity, Long> {
    Optional<PipelineRunEntity> findByRunId(String runId);
    boolean existsByRunId(String runId);
}
