package com.leets7th.job_is_be.domain.job.controller;

import com.leets7th.job_is_be.domain.job.dto.JobLinkValidateResponse;
import com.leets7th.job_is_be.domain.job.service.JobLinkValidationService;
import com.leets7th.job_is_be.global.response.ApiResponse;
import com.leets7th.job_is_be.global.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Job")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/jobs")
public class JobLinkValidationController {

    private final JobLinkValidationService jobLinkValidationService;

    @Operation(summary = "원문 링크 유효성 확인", description = "공고 ID를 받아 해당 공고의 원문 링크 접속 가능 여부를 검증합니다.")
    @GetMapping("/{jobId}/source-check")
    public ResponseEntity<ApiResponse<JobLinkValidateResponse>> checkJobLink(
            @PathVariable("jobId") Long jobId
    ) {
        JobLinkValidateResponse response = jobLinkValidationService.validateJobLink(jobId);
        return ApiResponse.success(SuccessStatus.JOB_SOURCE_CHECK_SUCCESS, response);
    }
}
