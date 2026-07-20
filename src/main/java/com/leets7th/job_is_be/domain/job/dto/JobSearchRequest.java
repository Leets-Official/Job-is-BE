package com.leets7th.job_is_be.domain.job.dto;

import lombok.Builder;

@Builder
public record JobSearchRequest(
        String keyword,
        String categoryChild,
        String skillTag,
        String region,
        int page,
        int size
) {
    public JobSearchRequest {
        if (page < 0) page = 0;
        if (size <= 0) size = 20;
    }
}
