package com.leets7th.job_is_be.domain.deck.dto;

public record DismissReasonRequest(
        String reasonCode, // 직무불일치, 지역, 경력요건, 회사규모, 이미지원함, 기타 등 (선택)
        String comment     // 자유 코멘트, 최대 200자 (선택)
) {
}
