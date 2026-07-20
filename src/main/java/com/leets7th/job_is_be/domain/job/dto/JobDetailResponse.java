package com.leets7th.job_is_be.domain.job.dto;

import lombok.Builder;
import java.time.OffsetDateTime;
import java.util.List;

@Builder
public record JobDetailResponse(
        Long id,
        String companyName,
        String position,
        String careerLevel,
        String employmentType,
        boolean remoteAvailable,
        String sourceUrl,
        OffsetDateTime dueTime,

        String intro,
        String mainTasks,
        String requirements,
        String preferredPoints,
        String benefits,

        Integer employeeCount,
        String companyType,
        String industry,
        String stockStatus,

        List<String> skillTags,
        String locationFull
) {}
