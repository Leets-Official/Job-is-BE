package com.leets7th.job_is_be.global.status;

import com.leets7th.job_is_be.global.base.BaseStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseStatus {

    // 예시
    COMM_ERROR_STATUS(HttpStatus.BAD_REQUEST, "COMM_400", "잘못된 요청입니다."),

    /**
     * Common
     */
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMM_400", "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMM_401", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMM_403", "접근 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMM_404", "요청한 자원을 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMM_405", "허용되지 않은 메소드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMM_500", "서버 내부 오류입니다."),

    /**
     * Auth
     */
    REFRESH_TOKEN_MISSING(HttpStatus.UNAUTHORIZED, "AUTH_401_1", "Refresh Token이 없습니다."),
    REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "AUTH_401_2", "유효하지 않은 Refresh Token입니다."),
    REFRESH_SESSION_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH_401_3", "Refresh Token 세션이 만료되었거나 로그아웃되었습니다."),
    DEV_LOGIN_USER_ID_REQUIRED(HttpStatus.BAD_REQUEST, "AUTH_400_1", "개발자 로그인에 사용할 userId가 필요합니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404_1", "사용자를 찾을 수 없습니다."),

    /**
     * Deck
     */
    DECK_NOT_FOUND(HttpStatus.NOT_FOUND, "DECK_404_1", "덱을 찾을 수 없습니다."),

    /**
     * Job
     */
    JOB_NOT_FOUND(HttpStatus.NOT_FOUND, "JOB_404_1", "공고를 찾을 수 없습니다."),
    JOB_ALREADY_SAVED(HttpStatus.CONFLICT, "JOB_409_1", "이미 저장된 공고입니다."),
    JOB_NOT_SAVED(HttpStatus.NOT_FOUND, "JOB_404_2", "저장되지 않은 공고입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
