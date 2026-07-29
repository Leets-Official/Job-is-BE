package com.leets7th.job_is_be.domain.job.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.time.OffsetDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CrawledJobDto(
        @JsonProperty("position") String title,           // JSON의 position -> title
        @JsonProperty("source_url") String detailUrl,      // JSON의 source_url -> detailUrl
        @JsonProperty("reward_total") String reward,       // JSON의 reward_total -> reward
        @JsonProperty("skill_tags") List<String> skills,
        CrawledCompanyDto company,
        List<String> categories,
        String intro,
        String mainTasks,
        String requirements,
        String preferredPoints,
        String benefits,
        Integer careerMin,
        Integer careerMax,
        String categoryName,
        String regionName,
        String source,
        Long externalId,
        String careerLevel,
        String employmentType,
        boolean remoteAvailable,
        OffsetDateTime postedAt,
        OffsetDateTime deadlineAt
) {}
