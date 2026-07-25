package com.leets7th.job_is_be.domain.job.dto;

import com.leets7th.job_is_be.domain.job.entity.Job;
import com.leets7th.job_is_be.domain.job.enums.TechStackType;
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
) {
    public static JobSummaryResponse from(Job job) {
        // 이미 List<String>이므로 별도 스트림 변환 없이 그대로 할당 가능
        List<String> tags = job.getSkillTags() != null ? job.getSkillTags() : List.of();

        return JobSummaryResponse.builder()
                .id(job.getId())
                .companyName(job.getCompany() != null ? job.getCompany().getName() : null)
                .position(job.getTitle())
                .careerLevel(job.getCareerLevel())
                .employmentType(job.getEmploymentType())
                .remoteAvailable(job.isRemoteAvailable())
                .dueTime(job.getDeadlineAt())
                .thumbnailUrl(null)
                .skillTags(tags)
                .build();
    }
}
