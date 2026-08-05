package com.leets7th.job_is_be.domain.job.controller;

import com.leets7th.job_is_be.domain.job.controller.docs.JobCrawlerControllerDocs;
import com.leets7th.job_is_be.domain.job.service.JobCrawlerManager;
import com.leets7th.job_is_be.domain.job.service.JobPostingSyncService;
import com.leets7th.job_is_be.global.response.ApiResponse;
import com.leets7th.job_is_be.global.status.SuccessStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/admin/crawler")
@RequiredArgsConstructor
public class JobCrawlerController implements JobCrawlerControllerDocs {

    private final JobCrawlerManager jobCrawlerManager;
    private final JobPostingSyncService jobPostingSyncService;

    @PostMapping("/run")
    public ResponseEntity<ApiResponse<String>> triggerCrawlerPipeline() {
        log.info("관리자 요청: 전체 수집 파이프라인 수동 실행 API 호출됨");
        jobCrawlerManager.runPipelineAndSave();
        return ApiResponse.success(
                SuccessStatus.CRAWLER_PIPELINE_RUN_SUCCESS,
                "크롤링 및 DB 적재 프로세스가 완료되었습니다. 서버 로그를 확인하세요."
        );
    }

    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<String>> syncJobPostings() {
        log.info("관리자 요청: job_postings → jobs 수동 동기화 시작");
        jobPostingSyncService.sync();
        return ApiResponse.success(
                SuccessStatus.JOB_POSTING_SYNC_SUCCESS,
                "job_postings → jobs 동기화가 완료되었습니다. 서버 로그를 확인하세요."
        );
    }
}
