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

    // ticker별 최신 run 조회 (브리핑 페이지용)
    @Query("SELECT r FROM PipelineRunEntity r WHERE r.executedAt = (SELECT MAX(r2.executedAt) FROM PipelineRunEntity r2 WHERE r2.ticker = r.ticker)")
    List<PipelineRunEntity> findLatestPerTicker();

    // 최근 N개 run (트랙레코드용)
    List<PipelineRunEntity> findTop50ByOrderByExecutedAtDesc();
}
