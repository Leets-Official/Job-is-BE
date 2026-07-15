package com.leets7th.job_is_be.global.status;

import com.leets7th.job_is_be.global.base.BaseStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessStatus implements BaseStatus {

    // 예시
    COMM_SUCCESS_STATUS(HttpStatus.OK, "COMM_200", "성공적으로 처리되었습니다."),
    TOKEN_REISSUE_SUCCESS(HttpStatus.OK, "AUTH_200_1", "토큰을 재발급했습니다."),
    SESSION_GET_SUCCESS(HttpStatus.OK, "AUTH_200_2", "세션 상태를 조회했습니다."),
    LOGOUT_SUCCESS(HttpStatus.OK, "AUTH_200_3", "로그아웃했습니다."),
    CSRF_TOKEN_GET_SUCCESS(HttpStatus.OK, "AUTH_200_4", "CSRF 토큰을 발급했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
