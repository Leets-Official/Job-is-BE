package com.leets7th.job_is_be.domain.job.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "원문 링크 유효성 검증 응답 DTO")
public class JobLinkValidateResponse {

    @Schema(description = "링크 유효 여부 (정상 접속 가능 시 true)", example = "true")
    @JsonProperty("isValid")
    private boolean isValid;

    @Schema(description = "HTTP 응답 상태 코드 (타임아웃/연결실패 시 null 가능)", example = "200")
    private Integer statusCode;

    @Schema(description = "검증 결과 메시지", example = "정상 접근 가능한 링크입니다.")
    private String message;

    public static JobLinkValidateResponse of(boolean isValid, Integer statusCode, String message) {
        return JobLinkValidateResponse.builder()
                .isValid(isValid)
                .statusCode(statusCode)
                .message(message)
                .build();
    }
}
