package com.leets7th.job_is_be.domain.job.controller;

import com.leets7th.job_is_be.domain.job.dto.JobDetailResponse;
import com.leets7th.job_is_be.domain.job.dto.JobSearchRequest;
import com.leets7th.job_is_be.domain.job.dto.JobSummaryResponse;
import com.leets7th.job_is_be.domain.job.service.JobService;
import com.leets7th.job_is_be.global.response.ApiResponse;
import com.leets7th.job_is_be.global.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    // TODO 인증 세팅 후 @AuthenticationPrincipal로 교체
    @PostMapping("/{jobId}/save")
    public ResponseEntity<ApiResponse<Void>> saveJob(
            @PathVariable Long jobId,
            @RequestParam Long userId
    ) {
        jobService.saveJob(userId, jobId);
        return ApiResponse.success(SuccessStatus.JOB_SAVE_SUCCESS);
    }

    // TODO 인증 세팅 후 @AuthenticationPrincipal로 교체
    @DeleteMapping("/{jobId}/save")
    public ResponseEntity<ApiResponse<Void>> unsaveJob(
            @PathVariable Long jobId,
            @RequestParam Long userId
    ) {
        jobService.unsaveJob(userId, jobId);
        return ApiResponse.success(SuccessStatus.JOB_UNSAVE_SUCCESS);
    }

    // 공고 탐색 및 검색
    @Operation(summary = "공고 탐색 및 검색", description = "필터 조건(직군, 지역, 경력 등)과 키워드를 기반으로 공고 목록을 페이징 조회합니다.")
    @GetMapping
    public ResponseEntity<Page<JobSummaryResponse>> searchJobs(
            @Valid @ModelAttribute @ParameterObject JobSearchRequest condition,
            @ParameterObject Pageable pageable
    ) {
        Page<JobSummaryResponse> response = jobService.searchJobs(condition, pageable);
        return ResponseEntity.ok(response);
    }

    //공고 상세 조회
    @Operation(summary = "공고 상세 조회", description = "external_id(예: 348736)를 통해 특정 공고의 상세 정보를 조회합니다.")
    @GetMapping("/{jobId}")
    public ResponseEntity<JobDetailResponse> getJobDetail(
            @Parameter(description = "공고 external_id") @PathVariable Long jobId
    ) {
        JobDetailResponse response = jobService.getJobDetail(jobId);
        return ResponseEntity.ok(response);
    }
}
