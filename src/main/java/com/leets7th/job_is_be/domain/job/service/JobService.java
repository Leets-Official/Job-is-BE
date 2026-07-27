package com.leets7th.job_is_be.domain.job.service;

import com.leets7th.job_is_be.domain.job.dto.JobDetailResponse;
import com.leets7th.job_is_be.domain.job.dto.JobSearchRequest;
import com.leets7th.job_is_be.domain.job.dto.JobSummaryResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    /**
     * 채용공고 탐색 및 검색
     */
    @Transactional(readOnly = true)
    public Page<JobSummaryResponse> searchJobs(JobSearchRequest condition, Pageable pageable) {
        Pageable fixedPageable = PageRequest.of(pageable.getPageNumber(), 24, pageable.getSort());
        return jobRepository.searchJobs(condition, fixedPageable);
    }

    /**
     * 채용공고 단건 상세 조회
     * - 존재하지 않을 경우 JOB_NOT_FOUND 예외 발생
     */
    @Transactional(readOnly = true)
    public JobDetailResponse getJobDetail(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.JOB_NOT_FOUND));

        return JobDetailResponse.from(job);
    }
}
