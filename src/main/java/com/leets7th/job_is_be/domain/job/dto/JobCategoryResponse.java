package com.leets7th.job_is_be.domain.job.dto;

import com.leets7th.job_is_be.domain.job.entity.JobCategory;
import lombok.Getter;

@Getter
public class JobCategoryResponse {
    private final Long id;
    private final String name;

    public JobCategoryResponse(JobCategory jobCategory) {
        this.id = jobCategory.getId();
        this.name = jobCategory.getName();
    }
}
