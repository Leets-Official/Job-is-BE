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
    OAUTH_PROVIDER_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "AUTH_400_1", "지원하지 않는 소셜 로그인 방식입니다."),
    OAUTH_CODE_MISSING(HttpStatus.BAD_REQUEST, "AUTH_400_2", "OAuth 인가 코드가 없습니다."),
    OAUTH_EMAIL_REQUIRED(HttpStatus.BAD_REQUEST, "AUTH_400_3", "소셜 계정의 이메일을 확인할 수 없습니다."),
    DEV_LOGIN_USER_ID_REQUIRED(HttpStatus.BAD_REQUEST, "AUTH_400_4", "개발자 로그인에 사용할 userId가 필요합니다."),
    OAUTH_LOGIN_CODE_MISSING(HttpStatus.BAD_REQUEST, "AUTH_400_5", "OAuth 로그인 코드가 없습니다."),
    OAUTH_STATE_INVALID(HttpStatus.UNAUTHORIZED, "AUTH_401_4", "유효하지 않거나 만료된 OAuth 요청입니다."),
    OAUTH_LOGIN_CODE_INVALID(HttpStatus.UNAUTHORIZED, "AUTH_401_5", "유효하지 않거나 만료된 OAuth 로그인 코드입니다."),
    SOCIAL_ACCOUNT_CONFLICT(HttpStatus.CONFLICT, "AUTH_409_1", "이미 다른 소셜 계정으로 가입된 이메일입니다."),
    WITHDRAWAL_RESTORE_EXPIRED(HttpStatus.FORBIDDEN, "AUTH_403_1", "계정 복구 가능 기간이 지났습니다."),
    OAUTH_PROVIDER_ERROR(HttpStatus.BAD_GATEWAY, "AUTH_502_1", "소셜 로그인 서버 연동에 실패했습니다."),
    OAUTH_USER_INFO_INVALID(HttpStatus.BAD_GATEWAY, "AUTH_502_2", "소셜 로그인 사용자 정보가 올바르지 않습니다."),
    OAUTH_NOT_CONFIGURED(HttpStatus.SERVICE_UNAVAILABLE, "AUTH_503_1", "소셜 로그인 설정이 완료되지 않았습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404_1", "사용자를 찾을 수 없습니다."),

    /**
     * Deck
     */
    DECK_NOT_FOUND(HttpStatus.NOT_FOUND, "DECK_404_1", "덱을 찾을 수 없습니다."),
    CARD_NOT_FOUND(HttpStatus.NOT_FOUND, "DECK_404_2", "카드를 찾을 수 없습니다."),
    CARD_NOT_DISMISSED(HttpStatus.BAD_REQUEST, "DECK_400_1", "관심없음 처리되지 않은 카드입니다."),
    CARD_ALREADY_DISMISSED(HttpStatus.BAD_REQUEST, "DECK_400_2", "이미 관심없음 처리된 카드입니다."),
    CARD_DISMISS_REASON_ALREADY_SUBMITTED(HttpStatus.BAD_REQUEST, "DECK_400_3", "이미 관심없음 사유를 제출한 카드입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
