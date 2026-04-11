package com.kyoung.signal.repository;

import com.kyoung.signal.domain.OutcomeRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OutcomeRecordRepository extends JpaRepository<OutcomeRecordEntity, Long> {
    Optional<OutcomeRecordEntity> findByRunId(String runId);
    List<OutcomeRecordEntity> findByTicker(String ticker);
}
