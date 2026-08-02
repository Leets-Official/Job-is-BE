package com.leets7th.job_is_be.domain.job.dto;

public record CriteriaMatrixDto(
        String jobType,    // 직무 판정 (✓ / ~ / !)
        String career,     // 경력 판정 (✓ / ~ / !)
        String location,   // 지역 판정 (✓ / ~ / !)
        String skills,     // 스킬 판정 (✓ / ~ / !)
        String preference, // 선호 조건 판정 (✓ / ~ / !)
        String salary      // 급여 판정 (미기재 시 항상 "!" 고정)
) {
    public static CriteriaMatrixDto createDefault() {
        return new CriteriaMatrixDto("~", "~", "~", "~", "~", "!");
    }
}
