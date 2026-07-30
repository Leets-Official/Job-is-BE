package com.leets7th.job_is_be.domain.deck.controller.docs;

import com.leets7th.job_is_be.domain.deck.dto.BriefingResponse;
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

@Tag(name = "Briefing", description = "오늘의 브리핑(추천 덱 요약) 조회 API")
@SecurityRequirement(name = "JWT")
public interface BriefingControllerDocs {

    @Operation(
            summary = "오늘의 브리핑 조회",
            description = "로그인한 사용자의 오늘 생성된 추천 덱 브리핑을 조회합니다. 사용자는 Access Token(JWT)으로 식별됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResponse<BriefingResponse>> getTodayBriefing(
            @Parameter(hidden = true) Jwt jwt
    );

    @Operation(
            summary = "오늘의 브리핑 카드 상태 조회",
            description = "로그인한 사용자의 오늘 브리핑에 포함된 카드들의 처리 상태(열람/관심없음 등)를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResponse<List<CardResponse>>> getTodayBriefingStatus(
            @Parameter(hidden = true) Jwt jwt
    );
}
