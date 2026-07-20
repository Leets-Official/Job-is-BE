package com.leets7th.job_is_be.domain.job.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CrawledCompanyDto(
        String name,        // 기업명
        String logoUrl,     // 로고 이미지 URL
        String description  // 기업 한 줄 소개 또는 설명
) {}
