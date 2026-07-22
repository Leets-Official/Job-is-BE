package com.leets7th.job_is_be.domain.job.controller;

import com.leets7th.job_is_be.domain.job.service.JobService;
import com.leets7th.job_is_be.global.response.ApiResponse;
import com.leets7th.job_is_be.global.status.SuccessStatus;
import lombok.RequiredArgsConstructor;
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
}
