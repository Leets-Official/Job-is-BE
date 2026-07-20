package com.leets7th.job_is_be.domain.deck.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * REC-01 인트로 화면 응답
 */
public record BriefingResponse(
        String greeting,
        long applicableCount, // 지원가능 건수 — 마감되지 않은 전체 공고 수
        int curatedCount,   // 추린 건수 — 오늘 덱에 담긴 카드 수
        String theme
) {
}
