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

    // 추천 파이프라인 수동 트리거. 화면설계서 REC §0.5의 06:00 자동 생성 배치는 아직 없어
    // 그때까지 이 엔드포인트로 오늘 덱을 생성한다.
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<List<CardResponse>>> generateTodayDeck(
            @AuthenticationPrincipal Jwt jwt
    ) {
        List<CardResponse> response = recommendationService.generateTodayDeck(Long.valueOf(jwt.getSubject()));
        return ApiResponse.success(SuccessStatus.DECK_GENERATE_SUCCESS, response);
    }
}
