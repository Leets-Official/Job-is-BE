package com.leets7th.job_is_be.domain.deck.dto;

import java.time.LocalDate;

/**
 * REC-01 인트로 화면 응답
 */
public record BriefingResponse(
        String greeting,
        long todayNewJobCount,
        int curatedCount,
        String theme,
        Long deckId,
        String slot,
        LocalDate deckDate
) {
}
