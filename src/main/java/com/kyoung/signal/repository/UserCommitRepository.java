package com.kyoung.signal.repository;

import com.kyoung.signal.domain.UserCommitEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserCommitRepository extends JpaRepository<UserCommitEntity, Long> {
    List<UserCommitEntity> findByRunIdOrderByCommittedAtDesc(String runId);
    Optional<UserCommitEntity> findFirstByRunIdOrderByCommittedAtDesc(String runId);
    List<UserCommitEntity> findByTicker(String ticker);
}
