package com.leets7th.job_is_be.domain.job.service;

import com.leets7th.job_is_be.domain.job.dto.CareerLevelResponse;
import com.leets7th.job_is_be.domain.job.dto.JobCategoryResponse;
import com.leets7th.job_is_be.domain.job.dto.RegionResponse;
import com.leets7th.job_is_be.domain.job.repository.JobCategoryRepository;
import com.leets7th.job_is_be.domain.job.repository.RegionRepository;
import com.leets7th.job_is_be.domain.user.enums.CareerLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MetadataQueryService {

    private final RegionRepository regionRepository;
    private final JobCategoryRepository jobCategoryRepository;

    public List<RegionResponse> getAllRegions() {
        return regionRepository.findAll().stream()
                .map(RegionResponse::new)
                .collect(Collectors.toList());
    }

    public List<JobCategoryResponse> getAllJobCategories() {
        return jobCategoryRepository.findAll().stream()
                .map(JobCategoryResponse::new)
                .collect(Collectors.toList());
    }

    public List<CareerLevelResponse> getAllCareerLevels() {
        return Arrays.stream(CareerLevel.values())
                .map(CareerLevelResponse::new)
                .collect(Collectors.toList());
    }
}
