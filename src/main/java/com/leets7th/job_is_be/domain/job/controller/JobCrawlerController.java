package com.leets7th.job_is_be.domain.job.controller;

import com.leets7th.job_is_be.domain.job.service.JobCrawlerManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/admin/crawler")
@RequiredArgsConstructor
public class JobCrawlerController {

    private final JobCrawlerManager jobCrawlerManager;

    /**
     * 전체 수집 및 DB 적재 파이프라인을 즉시 수동 실행.
     * 관리자용 엔드포인트.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/run")
    public ResponseEntity<String> triggerCrawlerPipeline() {
        log.info("관리자 요청: 전체 수집 파이프라인 수동 실행 API 호출됨");

        // 일단 확인하기 위해 동기식 호출로 처리합니다.
        jobCrawlerManager.runPipelineAndSave();

        return ResponseEntity.ok("크롤링 및 DB 적재 프로세스가 완료되었습니다. 서버 로그를 확인하세요.");
    }
}
