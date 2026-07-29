package com.leets7th.job_is_be.domain.job.controller;

import com.leets7th.job_is_be.domain.job.dto.CareerLevelResponse;
import com.leets7th.job_is_be.domain.job.dto.JobCategoryResponse;
import com.leets7th.job_is_be.domain.job.dto.RegionResponse;
import com.leets7th.job_is_be.domain.job.dto.TechStackResponse;
import com.leets7th.job_is_be.domain.job.enums.TechStackType;
import com.leets7th.job_is_be.domain.job.controller.docs.MetadataControllerDocs;
import com.leets7th.job_is_be.domain.job.service.MetadataQueryService;
import com.leets7th.job_is_be.global.response.ApiResponse;
import com.leets7th.job_is_be.global.status.SuccessStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/jobs/filters")
@RequiredArgsConstructor
public class MetadataController implements MetadataControllerDocs {

    private final MetadataQueryService metadataQueryService;

    @GetMapping("/regions")
    public ResponseEntity<List<RegionResponse>> getAllRegions() {
        List<RegionResponse> response = metadataQueryService.getAllRegions();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/job-categories")
    public ResponseEntity<List<JobCategoryResponse>> getAllJobCategories() {
        List<JobCategoryResponse> response = metadataQueryService.getAllJobCategories();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/career-levels")
    public ResponseEntity<List<CareerLevelResponse>> getAllCareerLevels() {
        return ResponseEntity.ok(metadataQueryService.getAllCareerLevels());
    }

    @GetMapping("/tech-stacks")
    public ResponseEntity<ApiResponse<List<TechStackResponse>>> getAllTechStacks() {
        List<TechStackResponse> response = Arrays.stream(TechStackType.values())
                .map(TechStackResponse::from)
                .toList();
        return ApiResponse.success(SuccessStatus.TECH_STACK_GET_SUCCESS, response);
    }
}
