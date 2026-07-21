package com.leets7th.job_is_be.domain.user.dto;

import com.leets7th.job_is_be.domain.user.enums.CareerLevel;
import com.leets7th.job_is_be.domain.user.enums.OnboardingStep;

import java.util.List;

public record ProfileDraftResponse(
        OnboardingStep onboardingStep,
        List<JobCategoryItem> jobCategories,
        RegionItem region,
        CareerLevel careerLevel,
        List<String> preferenceNotes,
        List<String> excludeKeywords,
        List<String> techStacks,
        boolean jobTestCompleted
) {
    public record JobCategoryItem(Long id, String name, boolean primary) {
    }

    public record RegionItem(Long id, String name) {
    }
}
