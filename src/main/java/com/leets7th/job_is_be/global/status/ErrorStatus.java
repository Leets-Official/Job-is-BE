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
    CONFLICT(HttpStatus.CONFLICT, "COMM_409", "다른 요청과 충돌했습니다. 다시 시도해 주세요."),
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
     * Profile
     */
    PROFILE_ONBOARDING_STEP_REQUIRED(HttpStatus.BAD_REQUEST, "PROFILE_400_1", "온보딩 진행 단계가 필요합니다."),
    PROFILE_JOB_CATEGORY_COUNT_INVALID(HttpStatus.BAD_REQUEST, "PROFILE_400_2", "관심 직무는 중복 없이 최대 3개까지 선택할 수 있습니다."),
    PROFILE_PRIMARY_JOB_CATEGORY_INVALID(HttpStatus.BAD_REQUEST, "PROFILE_400_3", "대표 관심 직무는 선택한 관심 직무에 포함되어야 합니다."),
    PROFILE_VALUE_TOO_LONG(HttpStatus.BAD_REQUEST, "PROFILE_400_4", "프로필 입력값이 허용 길이를 초과했습니다."),
    PROFILE_REQUIRED_FIELDS_MISSING(HttpStatus.BAD_REQUEST, "PROFILE_400_5", "온보딩 완료에 필요한 프로필 항목을 입력해 주세요."),
    PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "PROFILE_404_1", "완료된 프로필을 찾을 수 없습니다."),
    PROFILE_JOB_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "PROFILE_404_2", "선택한 관심 직무를 찾을 수 없습니다."),
    PROFILE_REGION_NOT_FOUND(HttpStatus.NOT_FOUND, "PROFILE_404_3", "선택한 희망 지역을 찾을 수 없습니다."),
    PROFILE_ALREADY_COMPLETED(HttpStatus.CONFLICT, "PROFILE_409_1", "이미 온보딩을 완료한 프로필입니다."),
    PROFILE_ONBOARDING_NOT_READY(HttpStatus.CONFLICT, "PROFILE_409_2", "온보딩 확인 단계에서 완료할 수 있습니다."),

    /**
     * Deck
     */
    DECK_NOT_FOUND(HttpStatus.NOT_FOUND, "DECK_404_1", "덱을 찾을 수 없습니다."),
    CARD_NOT_FOUND(HttpStatus.NOT_FOUND, "DECK_404_2", "카드를 찾을 수 없습니다."),
    CARD_NOT_DISMISSED(HttpStatus.BAD_REQUEST, "DECK_400_1", "관심없음 처리되지 않은 카드입니다."),
    CARD_ALREADY_DISMISSED(HttpStatus.BAD_REQUEST, "DECK_400_2", "이미 관심없음 처리된 카드입니다."),
    CARD_DISMISS_REASON_ALREADY_SUBMITTED(HttpStatus.BAD_REQUEST, "DECK_400_3", "이미 관심없음 사유를 제출한 카드입니다."),

    /**
     * Resume (이력서/자소서 파일)
     */
    RESUME_INVALID_FILE_FORMAT(HttpStatus.BAD_REQUEST, "RESUME_400_1", "지원하지 않는 파일 형식입니다. (PDF, DOCX, HWP, HWPX만 업로드 가능합니다)"),
    RESUME_OBJECT_KEY_MISMATCH(HttpStatus.BAD_REQUEST, "RESUME_400_2", "발급받은 업로드 경로와 일치하지 않습니다."),
    RESUME_UPLOAD_NOT_FOUND(HttpStatus.BAD_REQUEST, "RESUME_400_3", "S3에 업로드된 파일을 찾을 수 없습니다."),
    RESUME_FILE_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "RESUME_413_1", "파일 용량은 10MB를 초과할 수 없습니다."),
    RESUME_NOT_FOUND(HttpStatus.NOT_FOUND, "RESUME_404_1", "이력서/자소서 파일을 찾을 수 없습니다."),

    /**
     * Job
     */
    JOB_NOT_FOUND(HttpStatus.NOT_FOUND, "JOB_404_1", "공고를 찾을 수 없습니다."),
    JOB_ALREADY_SAVED(HttpStatus.CONFLICT, "JOB_409_1", "이미 저장된 공고입니다."),
    JOB_NOT_SAVED(HttpStatus.NOT_FOUND, "JOB_404_2", "저장되지 않은 공고입니다."),
    JOB_NOT_APPLICABLE(HttpStatus.BAD_REQUEST, "JOB_400_1", "마감되었거나 무효한 공고입니다."),

    /**
     * Notification
     */
    NOTIFICATION_INVALID_SEND_SLOT(HttpStatus.BAD_REQUEST, "NOTIFICATION_400_1", "허용되지 않는 발송 시간대입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
