package com.leets7th.job_is_be.domain.job.dto;

import lombok.Builder;
import java.time.OffsetDateTime;
import java.util.List;

@Builder
public record JobSummaryResponse(
        Long id,
        String companyName,
        String position,
        String careerLevel,
        String employmentType,
        boolean remoteAvailable,
        OffsetDateTime dueTime,
        String thumbnailUrl,
        List<String> skillTags
) {}
