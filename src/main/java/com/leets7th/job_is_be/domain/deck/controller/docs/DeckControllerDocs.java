package com.leets7th.job_is_be.domain.deck.controller.docs;

import com.leets7th.job_is_be.domain.deck.dto.CardResponse;
import com.leets7th.job_is_be.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

@Tag(name = "Deck", description = "추천 덱 생성 API")
@SecurityRequirement(name = "JWT")
public interface DeckControllerDocs {

    @Operation(
            summary = "오늘의 추천 덱 생성",
            description = "로그인한 사용자를 대상으로 오늘의 추천 덱을 생성합니다. "
                    + "성향 퀴즈 결과를 페르소나로 삼아 pgvector 기반 추천 엔진을 실행하고, "
                    + "적합도·추천 이유·요약이 채워진 카드를 만듭니다. "
                    + "퀴즈 미완료면 onboarding_incomplete, 후보 0건이면 no_candidates 로 기록되고 카드는 생성되지 않습니다. "
                    + "06:00 자동 생성 배치가 붙기 전까지 수동 트리거용으로 사용합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResponse<List<CardResponse>>> generateTodayDeck(
            @Parameter(hidden = true) Jwt jwt
    );
}
