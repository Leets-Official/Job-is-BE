package com.leets7th.job_is_be.domain.job.service;

import com.leets7th.job_is_be.domain.job.dto.CriteriaMatrixDto;
import com.leets7th.job_is_be.domain.job.dto.SimilarJobItemDto;
import com.leets7th.job_is_be.domain.job.dto.SimilarJobsResponseDto;
import com.leets7th.job_is_be.domain.job.entity.Job;
import com.leets7th.job_is_be.domain.job.enums.JobStatus;
import com.leets7th.job_is_be.domain.job.repository.JobRepository;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobSimilarService {

    private final JobRepository jobRepository;

    public SimilarJobsResponseDto getSimilarJobs(String jobId) {
        // 1. String -> Long ID 변환 및 대상 공고 조회
        Long targetId;
        try {
            targetId = Long.parseLong(jobId);
        } catch (NumberFormatException e) {
            throw new GeneralException(ErrorStatus.JOB_NOT_FOUND);
        }

        Job targetJob = jobRepository.findById(targetId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.JOB_NOT_FOUND));

        // 2. 온디맨드 후보 공고 조회 (대상 제외 상위 5건)
        List<Job> candidateJobs = jobRepository.findCandidateJobsExcludingTarget(
                targetId,
                JobStatus.ACTIVE,
                OffsetDateTime.now(),
                PageRequest.of(0, 5)
        );

        // 3. 점수 클램핑 및 매트릭스 계산 후 DTO 조립
        List<SimilarJobItemDto> items = candidateJobs.stream()
                .map(candidate -> convertToSimilarJobItem(targetJob, candidate, 0.85f))
                .toList();

        return SimilarJobsResponseDto.of(jobId, items);
    }

    private SimilarJobItemDto convertToSimilarJobItem(Job target, Job candidate, float rawScore) {
        // 점수 클램프 (0~100 정수)
        int fitScore = Math.max(0, Math.min(100, Math.round(rawScore * 100)));

        // Job.java 필드명 반영 매트릭스 비교
        String targetCategory = target.getJobCategory() != null ? target.getJobCategory().getName() : null;
        String candidateCategory = candidate.getJobCategory() != null ? candidate.getJobCategory().getName() : null;

        String targetRegion = target.getRegion() != null ? target.getRegion().getName() : null;
        String candidateRegion = candidate.getRegion() != null ? candidate.getRegion().getName() : null;

        CriteriaMatrixDto matrix = new CriteriaMatrixDto(
                evaluateMatch(targetCategory, candidateCategory),       // 직무
                evaluateMatch(target.getCareerLevel(), candidate.getCareerLevel()), // 경력
                evaluateMatch(targetRegion, candidateRegion),           // 지역
                "~",                                                     // 스킬
                "~",                                                     // 선호 조건
                "!"                                                     // 급여 (항상 !)
        );

        String companyName = candidate.getCompany() != null ? candidate.getCompany().getName() : "";
        String categoryName = candidateCategory != null ? candidateCategory : "관련";

        return new SimilarJobItemDto(
                String.valueOf(candidate.getId()),
                candidate.getTitle(),
                companyName,
                fitScore,
                categoryName + " 직무 및 유사 요구 조건 반영 추천",
                List.of("동일 직무 카테고리", "유사 경력 조건"),
                matrix
        );
    }

    private String evaluateMatch(Object targetVal, Object candidateVal) {
        if (targetVal == null || candidateVal == null) return "~";
        return Objects.equals(targetVal, candidateVal) ? "✓" : "~";
    }
}
