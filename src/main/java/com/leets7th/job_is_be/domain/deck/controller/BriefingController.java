package com.leets7th.job_is_be.domain.deck.controller;

import com.leets7th.job_is_be.domain.deck.dto.BriefingResponse;
import com.leets7th.job_is_be.domain.deck.dto.CardResponse;
import com.leets7th.job_is_be.domain.deck.service.BriefingService;
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
@RequestMapping("/api/briefings")
@RequiredArgsConstructor
public class BriefingController {

    private final BriefingService briefingService;

    @GetMapping("/today/{userId}")
    public ResponseEntity<ApiResponse<BriefingResponse>> getTodayBriefing(
            //TODO 인증관련 세팅 후 userDetail 또는 Authentication 객체에서 userId 가져오기
            @PathVariable Long userId
    ) {
        BriefingResponse response = briefingService.getTodayBriefing(userId);
        return ApiResponse.success(SuccessStatus.BRIEFING_TODAY_SUCCESS, response);
    }

    @GetMapping("/status/{userId}")
    public ResponseEntity<ApiResponse<List<CardResponse>>> getTodayBriefingStatus(
            //TODO 인증관련 세팅 후 userDetail 또는 Authentication 객체에서 userId 가져오기
            @PathVariable Long userId
    ) {
        List<CardResponse> response = briefingService.getTodayBriefingStatus(userId);
        return ApiResponse.success(SuccessStatus.BRIEFING_STATUS_SUCCESS, response);
    }
}
