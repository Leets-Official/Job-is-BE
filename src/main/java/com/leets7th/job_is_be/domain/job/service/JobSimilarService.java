package com.leets7th.job_is_be.domain.job.service;

import com.leets7th.job_is_be.domain.job.dto.CriteriaMatrixDto;
import com.leets7th.job_is_be.domain.job.dto.SimilarJobItemDto;
import com.leets7th.job_is_be.domain.job.dto.SimilarJobsResponseDto;
import com.leets7th.job_is_be.domain.job.entity.Job;
import com.leets7th.job_is_be.domain.job.enums.JobStatus;
import com.leets7th.job_is_be.domain.job.repository.JobRepository;
import com.leets7th.job_is_be.domain.personality.entity.PersonalityTest;
import com.leets7th.job_is_be.domain.personality.repository.PersonalityTestRepository;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobSimilarService {

    private final JobRepository jobRepository;
    private final PersonalityTestRepository personalityTestRepository;

    /**
     * 사용자의 성향 퀴즈 결과를 기반으로 맞춤 공고를 추천합니다.
     */
    public SimilarJobsResponseDto getRecommendedJobsByPersonality(Long userId) {
        // DB에서 사용자 성향 데이터 조회
        PersonalityTest userPersonality = personalityTestRepository.findByUserId(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.PERSONALITY_NOT_FOUND));

        // 후보 공고 조회
        List<Job> candidateJobs = jobRepository.findApplicableJobs(
                JobStatus.ACTIVE,
                OffsetDateTime.now(),
                PageRequest.of(0, 10)
        );

        // 성향 데이터 기반 DTO 변환
        List<SimilarJobItemDto> items = candidateJobs.stream()
                .map(job -> convertToJobItem(userPersonality, job))
                .toList();

        return SimilarJobsResponseDto.of(String.valueOf(userId), items);
    }

    private SimilarJobItemDto convertToJobItem(PersonalityTest userPersonality, Job candidateJob) {
        int fitScore = calculateFitScore(userPersonality, candidateJob);

        CriteriaMatrixDto matrix = new CriteriaMatrixDto(
                "✓",
                "✓",
                "~",
                "~",
                "~",
                "!"
        );

        String companyName = candidateJob.getCompany() != null ? candidateJob.getCompany().getName() : "";
        String reasonStr = "성향 분석(" + userPersonality.getResultType() + ") 기반 맞춤 공고";

        return new SimilarJobItemDto(
                String.valueOf(candidateJob.getId()),
                candidateJob.getTitle(),
                companyName,
                fitScore,
                reasonStr,
                List.of("성향 키워드 일치", "우선순위 직무 조건"),
                matrix
        );
    }

    private int calculateFitScore(PersonalityTest personality, Job job) {
        return 85;
    }
}
