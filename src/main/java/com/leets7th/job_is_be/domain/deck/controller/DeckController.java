package com.leets7th.job_is_be.domain.deck.controller;

import com.leets7th.job_is_be.domain.deck.dto.CardResponse;
import com.leets7th.job_is_be.domain.deck.service.RecommendationService;
import com.leets7th.job_is_be.global.response.ApiResponse;
import com.leets7th.job_is_be.global.status.SuccessStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/decks")
@RequiredArgsConstructor
public class DeckController {

    private final RecommendationService recommendationService;

    // TODO: 실제 추천엔진 붙기 전까지 스웨거로 파이프라인을 수동 트리거해서 확인하기 위한 임시 엔드포인트
    @PostMapping("/generate/{userId}")
    public ResponseEntity<ApiResponse<List<CardResponse>>> generateTodayDeck(
            //TODO 인증관련 세팅 후 userDetail 또는 Authentication 객체에서 userId 가져오기
            @PathVariable Long userId
    ) {
        List<CardResponse> response = recommendationService.generateTodayDeck(userId);
        return ApiResponse.success(SuccessStatus.DECK_GENERATE_SUCCESS, response);
    }
}
