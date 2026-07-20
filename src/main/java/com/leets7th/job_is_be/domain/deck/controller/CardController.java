package com.leets7th.job_is_be.domain.deck.controller;

import com.leets7th.job_is_be.domain.deck.dto.CardResponse;
import com.leets7th.job_is_be.domain.deck.service.CardService;
import com.leets7th.job_is_be.global.response.ApiResponse;
import com.leets7th.job_is_be.global.status.SuccessStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/decks")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @GetMapping("/{deckId}/cards")
    public ResponseEntity<ApiResponse<List<CardResponse>>> getDeckCards(
            //TODO 인증관련 세팅 후 요청자가 해당 deckId의 소유자인지 검증
            @PathVariable Long deckId
    ) {
        List<CardResponse> response = cardService.getDeckCards(deckId);
        return ApiResponse.success(SuccessStatus.DECK_CARDS_SUCCESS, response);
    }
}
