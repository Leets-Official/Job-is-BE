package com.leets7th.job_is_be.domain.job.dto;

import com.leets7th.job_is_be.domain.job.entity.Job;
import lombok.Builder;
import java.time.OffsetDateTime;
import java.util.List;

@Builder
public record JobDetailResponse (
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
) {
    public static JobDetailResponse from(Job job) {
        return JobDetailResponse.builder()
                .id(job.getId())
                .companyName(job.getCompany() != null ? job.getCompany().getName() : null)
                .position(job.getTitle())
                .careerLevel(job.getCareerLevel())
                .employmentType(job.getEmploymentType())
                .remoteAvailable(job.isRemoteAvailable())
                .sourceUrl(job.getSourceUrl())
                .dueTime(job.getDeadlineAt())
                .mainTasks(job.getMainTasks())
                .requirements(job.getRequirements())
                .preferredPoints(job.getPreferredPoints())
                .skillTags(job.getSkillTags())
                .locationFull(job.getLocationFull())
                .build();
    }
}
