package com.leets7th.job_is_be.domain.job.dto;

import com.leets7th.job_is_be.domain.job.enums.TechStackType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
public record JobSearchRequest(
        String keyword,
        String categoryChild,

        @Schema(description = "스킬 태그 목록")
        List<TechStackType> skillTags,

        @Schema(description = "지역 목록", allowableValues = {"서울", "경기", "인천", "부산"})
        List<String> regions,

        Integer page,
        Integer size

        // ex) 최신 등록순: createdAt,desc
        // ex) 마감일 임박순 deadlineAt, asc
) {}
