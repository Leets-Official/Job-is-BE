package com.leets7th.job_is_be.domain.job.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "JobCrawler(Admin)", description = "공고 수집 파이프라인 수동 실행 관리자 API")
@SecurityRequirement(name = "JWT")
public interface JobCrawlerControllerDocs {

    @Operation(
            summary = "크롤링 파이프라인 수동 실행",
            description = "전체 공고 수집 및 DB 적재 파이프라인을 즉시 동기 실행합니다. ADMIN 권한이 필요합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "실행 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "ADMIN 권한 필요")
    })
    ResponseEntity<String> triggerCrawlerPipeline();
}
