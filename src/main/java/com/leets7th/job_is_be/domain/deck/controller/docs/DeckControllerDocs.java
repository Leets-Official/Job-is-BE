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
                    + "추천 엔진 연동 전까지 수동으로 파이프라인을 트리거하기 위한 엔드포인트입니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResponse<List<CardResponse>>> generateTodayDeck(
            @Parameter(hidden = true) Jwt jwt
    );
}
