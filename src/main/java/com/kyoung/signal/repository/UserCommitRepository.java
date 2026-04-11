package com.kyoung.signal.repository;

import com.kyoung.signal.domain.UserCommitEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserCommitRepository extends JpaRepository<UserCommitEntity, Long> {
    List<UserCommitEntity> findByRunId(String runId);
    List<UserCommitEntity> findByTicker(String ticker);
}
