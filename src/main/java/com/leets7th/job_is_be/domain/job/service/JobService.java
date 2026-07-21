package com.leets7th.job_is_be.domain.job.service;

import com.leets7th.job_is_be.domain.job.entity.Job;
import com.leets7th.job_is_be.domain.job.entity.SavedJob;
import com.leets7th.job_is_be.domain.job.repository.JobRepository;
import com.leets7th.job_is_be.domain.job.repository.SavedJobRepository;
import com.leets7th.job_is_be.domain.user.entity.User;
import com.leets7th.job_is_be.domain.user.repository.UserRepository;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final SavedJobRepository savedJobRepository;
    private final UserRepository userRepository;

    @Transactional
    public void saveJob(Long userId, Long jobId) {
        if (savedJobRepository.existsByUserIdAndJobId(userId, jobId)) {
            throw new GeneralException(ErrorStatus.JOB_ALREADY_SAVED);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.JOB_NOT_FOUND));

        try {
            savedJobRepository.save(SavedJob.builder()
                    .user(user)
                    .job(job)
                    .savedAt(OffsetDateTime.now())
                    .build());
        } catch (DataIntegrityViolationException e) {
            throw new GeneralException(ErrorStatus.JOB_ALREADY_SAVED);
        }
    }

    @Transactional
    public void unsaveJob(Long userId, Long jobId) {
        if (!savedJobRepository.existsByUserIdAndJobId(userId, jobId)) {
            throw new GeneralException(ErrorStatus.JOB_NOT_SAVED);
        }

        savedJobRepository.deleteByUserIdAndJobId(userId, jobId);
    }
}
