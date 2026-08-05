package com.leets7th.job_is_be.domain.job.repository;

import com.leets7th.job_is_be.domain.job.dto.JobFitSignalsDto;
import com.leets7th.job_is_be.domain.job.dto.JobSearchRequest;
import com.leets7th.job_is_be.domain.job.dto.JobSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface JobRepositoryCustom {
    /**
     * @param pythonFitScores 추천순(FIT)에서 파이썬 매칭 엔진이 산출한 점수(externalId → 0~100).
     *                         비어있으면 자바 규칙 기반 점수로 대체한다.
     */
    Page<JobSummaryResponse> searchJobs(JobSearchRequest request, Pageable pageable, JobFitSignalsDto fitSignals,
                                         Map<Long, Integer> pythonFitScores);
}
