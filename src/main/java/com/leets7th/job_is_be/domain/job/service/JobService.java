package com.leets7th.job_is_be.domain.job.service;

import com.leets7th.job_is_be.domain.deck.enums.ActionType;
import com.leets7th.job_is_be.domain.deck.repository.UserActionRepository;
import com.leets7th.job_is_be.domain.job.dto.SavedJobListResponse;
import com.leets7th.job_is_be.domain.job.dto.SavedJobResponse;
import com.leets7th.job_is_be.domain.job.entity.Job;
import com.leets7th.job_is_be.domain.job.entity.SavedJob;
import com.leets7th.job_is_be.domain.job.enums.JobStatus;
import com.leets7th.job_is_be.domain.job.enums.SavedJobSortType;
import com.leets7th.job_is_be.domain.job.repository.JobRepository;
import com.leets7th.job_is_be.domain.job.repository.SavedJobRepository;
import com.leets7th.job_is_be.domain.user.entity.User;
import com.leets7th.job_is_be.domain.user.repository.UserRepository;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.response.PageResponse;
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
    private final UserActionRepository userActionRepository;

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

    @Transactional(readOnly = true)
    public SavedJobListResponse getSavedJobs(Long userId, int page, int size, SavedJobSortType sortType) {
        // page는 클라이언트에는 1부터 시작하는 값으로 노출하고, Spring Data Pageable(0-based)로는 내부에서만 변환한다.
        Pageable pageable = PageRequest.of(page - 1, size);
        OffsetDateTime now = OffsetDateTime.now();

        Page<SavedJob> savedJobs = switch (sortType) {
            case SAVED_DESC -> savedJobRepository.findByUserIdOrderBySavedDesc(userId, now, pageable);
            case DEADLINE_ASC -> savedJobRepository.findByUserIdOrderByDeadlineAsc(userId, now, pageable);
        };

        Page<SavedJobResponse> responses = savedJobs.map(savedJob -> toSavedJobResponse(userId, savedJob, now));

        long totalSaved = savedJobRepository.countByUserId(userId);
        long totalApplyIntent = savedJobRepository.countApplyIntentByUserId(userId);

        PageResponse<SavedJobResponse> pageResponse = new PageResponse<>(
                responses.getContent(),
                page,
                responses.getSize(),
                responses.getTotalElements(),
                responses.getTotalPages(),
                responses.isLast()
        );

        return new SavedJobListResponse(totalSaved, totalApplyIntent, pageResponse);
    }

    private SavedJobResponse toSavedJobResponse(Long userId, SavedJob savedJob, OffsetDateTime now) {
        Job job = savedJob.getJob();
        boolean expired = job.getStatus() != JobStatus.ACTIVE
                || (job.getDeadlineAt() != null && !job.getDeadlineAt().isAfter(now));
        boolean applyIntent = userActionRepository.existsByUserIdAndJobIdAndActionType(
                userId, job.getId(), ActionType.APPLY_INTENT_CLICKED);

        return new SavedJobResponse(
                job.getId(),
                job.getCompany() != null ? job.getCompany().getName() : null,
                job.getTitle(),
                job.getLocationFull(),
                job.getCareerLevel(),
                job.getEmploymentType(),
                savedJob.getSavedAt(),
                job.getDeadlineAt(),
                expired,
                applyIntent
        );
    }
}
