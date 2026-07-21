package com.leets7th.job_is_be.domain.job.repository;

import com.leets7th.job_is_be.domain.job.entity.SavedJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {

    boolean existsByUserIdAndJobId(Long userId, Long jobId);

    Optional<SavedJob> findByUserIdAndJobId(Long userId, Long jobId);

    void deleteByUserIdAndJobId(Long userId, Long jobId);
}
