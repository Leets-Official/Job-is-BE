package com.leets7th.job_is_be.domain.job.controller;

import com.leets7th.job_is_be.domain.job.dto.CareerLevelResponse;
import com.leets7th.job_is_be.domain.job.dto.JobCategoryResponse;
import com.leets7th.job_is_be.domain.job.dto.RegionResponse;
import com.leets7th.job_is_be.domain.job.dto.TechStackResponse;
import com.leets7th.job_is_be.domain.job.enums.TechStackType;
import com.leets7th.job_is_be.domain.job.service.MetadataQueryService;
import com.leets7th.job_is_be.global.response.ApiResponse;
import com.leets7th.job_is_be.global.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@Tag(name = "Metadata", description = "지역, 직무, 경력 수준 카테고리 메타데이터 조회 API")
@RestController
@RequestMapping("/api/jobs/filters")
@RequiredArgsConstructor
public class MetadataController {

    private final MetadataQueryService metadataQueryService;

    @Operation(summary = "지역 목록 조회", description = "DB에 등록된 지역 메타데이터 목록을 조회합니다.")
    @GetMapping("/regions")
    public ResponseEntity<List<RegionResponse>> getAllRegions() {
        List<RegionResponse> response = metadataQueryService.getAllRegions();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "직무 카테고리 목록 조회", description = "DB에 등록된 직무 카테고리 메타데이터 목록을 조회합니다.")
    @GetMapping("/job-categories")
    public ResponseEntity<List<JobCategoryResponse>> getAllJobCategories() {
        List<JobCategoryResponse> response = metadataQueryService.getAllJobCategories();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "경력 수준 목록 조회", description = "시스템에 정의된 경력 수준 메타데이터를 조회합니다.")
    @GetMapping("/career-levels")
    public ResponseEntity<List<CareerLevelResponse>> getAllCareerLevels() {
        return ResponseEntity.ok(metadataQueryService.getAllCareerLevels());
    }

    @Operation(summary = "기술 스택 목록 조회", description = "시스템에 정의된 전체 기술 스택 메타데이터 목록을 조회합니다.")
    @GetMapping("/tech-stacks")
    public ResponseEntity<ApiResponse<List<TechStackResponse>>> getAllTechStacks() {
        List<TechStackResponse> response = Arrays.stream(TechStackType.values())
                .map(TechStackResponse::from)
                .toList();
        return ApiResponse.success(SuccessStatus.TECH_STACK_GET_SUCCESS, response);
    }
}
