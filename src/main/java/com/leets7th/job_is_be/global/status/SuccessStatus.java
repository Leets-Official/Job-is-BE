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

    /**
     * Auth
     */
    TOKEN_REISSUE_SUCCESS(HttpStatus.OK, "AUTH_200_1", "토큰을 재발급했습니다."),
    SESSION_GET_SUCCESS(HttpStatus.OK, "AUTH_200_2", "세션 상태를 조회했습니다."),
    LOGOUT_SUCCESS(HttpStatus.OK, "AUTH_200_3", "로그아웃했습니다."),
    CSRF_TOKEN_GET_SUCCESS(HttpStatus.OK, "AUTH_200_4", "CSRF 토큰을 발급했습니다."),
    DEV_LOGIN_SUCCESS(HttpStatus.OK, "AUTH_200_5", "개발자 전용 로그인에 성공했습니다."),
    OAUTH_EXCHANGE_SUCCESS(HttpStatus.OK, "AUTH_200_6", "로그인 토큰 교환에 성공했습니다."),

    /**
     * Profile
     */
    PROFILE_GET_SUCCESS(HttpStatus.OK, "PROFILE_200_1", "프로필을 조회했습니다."),
    PROFILE_UPDATE_SUCCESS(HttpStatus.OK, "PROFILE_200_2", "프로필을 수정했습니다."),
    PROFILE_DRAFT_GET_SUCCESS(HttpStatus.OK, "PROFILE_200_3", "온보딩 임시저장을 조회했습니다."),
    PROFILE_DRAFT_SAVE_SUCCESS(HttpStatus.OK, "PROFILE_200_4", "온보딩 내용을 임시저장했습니다."),
    PROFILE_ONBOARDING_COMPLETE_SUCCESS(HttpStatus.OK, "PROFILE_200_5", "온보딩을 완료했습니다."),


    /**
     * Deck
     */
    DECK_GENERATE_SUCCESS(HttpStatus.OK, "DECK_200_2", "오늘의 추천 덱을 생성했습니다."),
    CARD_DISMISS_SUCCESS(HttpStatus.OK, "DECK_200_3", "카드를 관심없음으로 처리했습니다."),
    CARD_DISMISS_CANCEL_SUCCESS(HttpStatus.OK, "DECK_200_4", "카드의 관심없음 처리를 취소했습니다."),
    CARD_DISMISS_REASON_SUCCESS(HttpStatus.OK, "DECK_200_5", "관심없음 사유를 제출했습니다."),

    /**
     * Briefing
     */
    BRIEFING_TODAY_SUCCESS(HttpStatus.OK, "BRIEFING_200", "오늘의 브리핑을 조회했습니다."),
    BRIEFING_STATUS_SUCCESS(HttpStatus.OK, "BRIEFING_200_2", "오늘의 브리핑 덱 카드 목록을 조회했습니다."),

    /**
     * Resume (이력서/자소서 파일)
     */
    RESUME_PRESIGNED_URL_SUCCESS(HttpStatus.OK, "RESUME_200_1", "업로드용 Presigned URL을 발급했습니다."),
    RESUME_UPLOAD_CONFIRM_SUCCESS(HttpStatus.CREATED, "RESUME_201_1", "이력서/자소서 파일을 저장했습니다."),
    RESUME_LIST_SUCCESS(HttpStatus.OK, "RESUME_200_2", "이력서/자소서 파일 목록을 조회했습니다."),
    RESUME_DELETE_SUCCESS(HttpStatus.OK, "RESUME_200_3", "이력서/자소서 파일을 삭제했습니다."),
    RESUME_UPLOAD_UPDATE_SUCCESS(HttpStatus.OK, "RESUME_200_4", "이력서/자소서 파일을 갱신했습니다."),

    /**
     * Job
     */
    JOB_SAVE_SUCCESS(HttpStatus.CREATED, "JOB_201_1", "공고를 저장했습니다."),
    JOB_UNSAVE_SUCCESS(HttpStatus.OK, "JOB_200_1", "공고 저장을 취소했습니다."),
    JOB_VIEW_RECORD_SUCCESS(HttpStatus.OK, "JOB_200_2", "공고 열람을 기록했습니다."),
    JOB_APPLY_INTENT_TOGGLE_SUCCESS(HttpStatus.OK, "JOB_200_3", "지원 의향 상태를 변경했습니다."),
    JOB_APPLY_RECORD_SUCCESS(HttpStatus.OK, "JOB_200_4", "지원하기 클릭을 기록했습니다."),
    SAVE_LIST_GET_SUCCESS(HttpStatus.OK, "JOB_200_5", "저장 목록을 조회했습니다."),
    JOB_SEARCH_SUCCESS(HttpStatus.OK, "JOB_200_2", "공고 목록을 조회했습니다."),
    JOB_DETAIL_SUCCESS(HttpStatus.OK, "JOB_200_3", "공고 상세 정보를 조회했습니다."),

    /**
     * Metadata / TechStack
     */
    TECH_STACK_GET_SUCCESS(HttpStatus.OK, "TECH_200_1", "기술 스택 목록을 조회했습니다."),

    /**
     * Notification
     */
    NOTIFICATION_SETTING_GET_SUCCESS(HttpStatus.OK, "NOTIFICATION_200_1", "알림 수신 설정을 조회했습니다."),
    NOTIFICATION_SETTING_UPDATE_SUCCESS(HttpStatus.OK, "NOTIFICATION_200_2", "알림 수신 설정을 변경했습니다."),
    NOTIFICATION_SNOOZE_SUCCESS(HttpStatus.OK, "NOTIFICATION_200_3", "알림 발송을 스누즈 설정했습니다."),
    NOTIFICATION_SNOOZE_CANCEL_SUCCESS(HttpStatus.OK, "NOTIFICATION_200_4", "알림 스누즈를 해제했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
