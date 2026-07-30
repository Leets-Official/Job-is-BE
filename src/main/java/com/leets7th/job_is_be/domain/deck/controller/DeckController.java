package com.leets7th.job_is_be.domain.deck.controller;

import com.leets7th.job_is_be.domain.deck.dto.CardResponse;
import com.leets7th.job_is_be.domain.deck.service.RecommendationService;
import com.leets7th.job_is_be.global.response.ApiResponse;
import com.leets7th.job_is_be.domain.deck.controller.docs.DeckControllerDocs;
import com.leets7th.job_is_be.global.status.SuccessStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/decks")
@RequiredArgsConstructor
public class DeckController implements DeckControllerDocs {

    private final RecommendationService recommendationService;

    // TODO: 실제 추천엔진 붙기 전까지 스웨거로 파이프라인을 수동 트리거해서 확인하기 위한 임시 엔드포인트
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<List<CardResponse>>> generateTodayDeck(
            @AuthenticationPrincipal Jwt jwt
    ) {
        List<CardResponse> response = recommendationService.generateTodayDeck(Long.valueOf(jwt.getSubject()));
        return ApiResponse.success(SuccessStatus.DECK_GENERATE_SUCCESS, response);
    }
}
