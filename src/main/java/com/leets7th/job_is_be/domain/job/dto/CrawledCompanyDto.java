package com.leets7th.job_is_be.domain.job.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CrawledCompanyDto {
    private String name;        // 기업명
    private String logoUrl;     // 로고 이미지 URL
    private String description; // 기업 한 줄 소개 또는 설명
}
