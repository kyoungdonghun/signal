package com.kyoung.signal.repository;

import com.kyoung.signal.domain.SchedulerLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SchedulerLogRepository extends JpaRepository<SchedulerLogEntity, Long> {
    List<SchedulerLogEntity> findTop20ByOrderByStartedAtDesc();
}
