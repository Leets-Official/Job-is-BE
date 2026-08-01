package com.leets7th.job_is_be.domain.user.dto;

import com.leets7th.job_is_be.domain.user.enums.CareerLevel;
import com.leets7th.job_is_be.domain.user.enums.OnboardingStep;

import java.util.List;

/**
 * 온보딩 중간 저장 요청. null 필드는 "변경 없음"을 뜻한다.
 * 희망 지역은 1~3개 다중 선택이며 원격 포함 여부는 별도 토글이다(PRO-01 필드 표).
 */
public record ProfileDraftRequest(
        OnboardingStep onboardingStep,
        List<Long> jobCategoryIds,
        Long primaryJobCategoryId,
        List<Long> regionIds,
        Boolean remoteOk,
        CareerLevel careerLevel,
        List<String> preferenceNotes,
        List<String> excludeKeywords,
        List<String> techStacks
) {
}
