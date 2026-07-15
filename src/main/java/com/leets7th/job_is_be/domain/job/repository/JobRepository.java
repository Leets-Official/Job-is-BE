package com.leets7th.job_is_be.domain.job.repository;

import com.leets7th.job_is_be.domain.job.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface JobRepository extends JpaRepository<Job, Long> {

    long countByPostedAtBetween(LocalDateTime start, LocalDateTime end);
}
