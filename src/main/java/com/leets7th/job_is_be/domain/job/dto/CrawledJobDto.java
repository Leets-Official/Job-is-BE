package com.leets7th.job_is_be.domain.job.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.time.OffsetDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CrawledJobDto(
        String title,
        String detailUrl,
        String reward,
        String locationDescription,
        CrawledCompanyDto company,
        List<String> categories,
        List<String> skills,
        String intro,
        String mainTasks,
        String requirements,
        String preferredPoints,
        String benefits,
        Integer careerMin,
        Integer careerMax,
        String categoryName,
        String regionName,
        String source,
        Long externalId,
        String careerLevel,
        String employmentType,
        boolean remoteAvailable,
        OffsetDateTime postedAt,
        OffsetDateTime deadlineAt
) {}
