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
    DECK_CARDS_SUCCESS(HttpStatus.OK, "DECK_200", "덱의 카드들을 조회했습니다."),
    DECK_GENERATE_SUCCESS(HttpStatus.OK, "DECK_200_2", "오늘의 추천 덱을 생성했습니다."),

    /**
     * Briefing
     */
    BRIEFING_TODAY_SUCCESS(HttpStatus.OK, "BRIEFING_200", "오늘의 브리핑을 조회했습니다."),
    BRIEFING_STATUS_SUCCESS(HttpStatus.OK, "BRIEFING_200_2", "오늘의 브리핑 덱 카드 목록을 조회했습니다."),

    /**
     * Job
     */
    JOB_SAVE_SUCCESS(HttpStatus.CREATED, "JOB_201_1", "공고를 저장했습니다."),
    JOB_UNSAVE_SUCCESS(HttpStatus.OK, "JOB_200_1", "공고 저장을 취소했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
