package com.kyoung.signal.repository;

import com.kyoung.signal.domain.PipelineRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PipelineRunRepository extends JpaRepository<PipelineRunEntity, Long> {
    Optional<PipelineRunEntity> findByRunId(String runId);
    boolean existsByRunId(String runId);

    // outcome 채움 대상: 특정 시간 범위에 실행된 run 중 아직 outcome이 없는 것
    @Query("""
        SELECT r FROM PipelineRunEntity r
        WHERE r.executedAt BETWEEN :from AND :to
        AND r.runId NOT IN (SELECT o.runId FROM OutcomeRecordEntity o)
    """)
    List<PipelineRunEntity> findRunsWithoutOutcome(
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to
    );
}
