package com.leets7th.job_is_be.domain.job.dto;

import com.leets7th.job_is_be.domain.user.enums.CareerLevel;

import java.util.List;

/**
 * 탐색 목록 "추천순"(FIT) 정렬에 쓰는 로그인 사용자 개인화 신호(화면설계서 EXP §2.1).
 * 후보 범위 자체는 좁히지 않고, 정렬 점수 계산에만 쓴다. 비로그인이면 이 객체 자체가 없다(null).
 */
public record JobFitSignalsDto(
        List<Long> jobCategoryIds,
        String regionName,
        boolean remoteOk,
        CareerLevel careerLevel,
        List<String> techStacks
) {
}
