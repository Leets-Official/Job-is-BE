package com.leets7th.job_is_be.domain.job.dto;

import java.util.List;

public record SimilarJobsResponseDto(
        String targetJobId,
        String label,
        List<SimilarJobItemDto> items
) {
    public static SimilarJobsResponseDto of(String targetJobId, List<SimilarJobItemDto> items) {
        return new SimilarJobsResponseDto(targetJobId, "추천", items);
    }
}
