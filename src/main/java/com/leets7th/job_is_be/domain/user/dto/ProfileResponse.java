package com.leets7th.job_is_be.domain.user.dto;

import com.leets7th.job_is_be.domain.user.enums.CareerLevel;

import java.time.LocalDateTime;
import java.util.List;

public record ProfileResponse(
        Long userId,
        List<ProfileJobCategoryResponse> jobCategories,
        ProfileRegionResponse region,
        CareerLevel careerLevel,
        List<String> preferenceNotes,
        List<String> excludeKeywords,
        List<String> techStacks,
        List<String> personalityTags,
        boolean jobTestCompleted,
        boolean onboardingCompleted,
        LocalDateTime onboardingCompletedAt
) {
}
