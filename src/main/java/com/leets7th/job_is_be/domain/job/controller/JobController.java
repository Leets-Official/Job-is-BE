package com.leets7th.job_is_be.domain.job.controller;

import com.leets7th.job_is_be.domain.job.controller.docs.JobControllerDocs;
import com.leets7th.job_is_be.domain.job.dto.JobDetailResponse;
import com.leets7th.job_is_be.domain.job.dto.JobSearchRequest;
import com.leets7th.job_is_be.domain.job.dto.JobSummaryResponse;
import com.leets7th.job_is_be.domain.job.dto.SimilarJobsResponseDto;
import com.leets7th.job_is_be.domain.job.service.JobService;
import com.leets7th.job_is_be.domain.job.service.JobSimilarService;
import com.leets7th.job_is_be.global.response.ApiResponse;
import com.leets7th.job_is_be.global.response.PageResponse;
import com.leets7th.job_is_be.global.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController implements JobControllerDocs {

    private final JobService jobService;
    private final JobSimilarService jobSimilarService;

    @PostMapping("/{jobId}/save")
    public ResponseEntity<ApiResponse<Void>> saveJob(
            @PathVariable Long jobId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        jobService.saveJob(Long.valueOf(jwt.getSubject()), jobId);
        return ApiResponse.success(SuccessStatus.JOB_SAVE_SUCCESS);
    }

    @DeleteMapping("/{jobId}/save")
    public ResponseEntity<ApiResponse<Void>> unsaveJob(
            @PathVariable Long jobId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        jobService.unsaveJob(Long.valueOf(jwt.getSubject()), jobId);
        return ApiResponse.success(SuccessStatus.JOB_UNSAVE_SUCCESS);
    }

    // 공고 탐색 및 검색
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<JobSummaryResponse>>> searchJobs(
            @Valid @ModelAttribute @ParameterObject JobSearchRequest condition,
            @PageableDefault(page = 0, size = 24) @ParameterObject Pageable pageable
    ) {
        Page<JobSummaryResponse> response = jobService.searchJobs(condition, pageable);
        return ApiResponse.success(SuccessStatus.JOB_SEARCH_SUCCESS, PageResponse.from(response));
    }

    // 공고 상세 조회
    @GetMapping("/{jobId}")
    public ResponseEntity<ApiResponse<JobDetailResponse>> getJobDetail(
            @PathVariable Long jobId
    ) {
        JobDetailResponse response = jobService.getJobDetail(jobId);
        return ApiResponse.success(SuccessStatus.JOB_DETAIL_SUCCESS, response);
    }

    /**
     * 로그인 사용자의 성향 퀴즈 결과 기반 추천. 특정 공고를 기준으로 하지 않으므로 jobId 를 받지 않는다.
     */
    @Operation(
            summary = "성향 기반 추천 공고 조회",
            description = """
                    로그인 사용자의 성향 퀴즈 결과로 추천 공고를 조회한다. 사용자 식별은 JWT로 하므로 요청 파라미터는 없다.
                    ※ '특정 공고와 비슷한 공고'를 찾는 API가 아니다. 기준은 공고가 아니라 사용자다.
                    """
    )
    @GetMapping("/similar")
    public ResponseEntity<ApiResponse<SimilarJobsResponseDto>> getRecommendedJobsByPersonality(
            @AuthenticationPrincipal Jwt jwt
    ) {
        SimilarJobsResponseDto response =
                jobSimilarService.getRecommendedJobsByPersonality(Long.valueOf(jwt.getSubject()));
        return ApiResponse.success(SuccessStatus.JOB_SIMILAR_SUCCESS, response);
    }
}
