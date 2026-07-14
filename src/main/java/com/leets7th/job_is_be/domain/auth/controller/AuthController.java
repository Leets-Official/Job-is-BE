package com.leets7th.job_is_be.domain.auth.controller;

import com.leets7th.job_is_be.domain.auth.dto.SessionResponse;
import com.leets7th.job_is_be.domain.auth.dto.TokenReissueResponse;
import com.leets7th.job_is_be.domain.auth.service.AuthService;
import com.leets7th.job_is_be.domain.auth.service.RefreshTokenCookieManager;
import com.leets7th.job_is_be.global.response.ApiResponse;
import com.leets7th.job_is_be.global.status.SuccessStatus;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenCookieManager cookieManager;

    public AuthController(AuthService authService, RefreshTokenCookieManager cookieManager) {
        this.authService = authService;
        this.cookieManager = cookieManager;
    }

    @PostMapping("/token/reissue")
    public ResponseEntity<ApiResponse<TokenReissueResponse>> reissue(HttpServletRequest request) {
        AuthService.ReissueResult result = authService.reissue(cookieManager.extract(request));

        return ResponseEntity
                .status(SuccessStatus.TOKEN_REISSUE_SUCCESS.getHttpStatus())
                .header(HttpHeaders.SET_COOKIE, cookieManager.create(result.refreshToken()).toString())
                .body(new ApiResponse<>(
                        true,
                        SuccessStatus.TOKEN_REISSUE_SUCCESS.getCode(),
                        SuccessStatus.TOKEN_REISSUE_SUCCESS.getMessage(),
                        result.response()
                ));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<SessionResponse>> me(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(
                SuccessStatus.SESSION_GET_SUCCESS,
                authService.getSession(Long.valueOf(jwt.getSubject()))
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        authService.logout(cookieManager.extract(request));

        return ResponseEntity
                .status(SuccessStatus.LOGOUT_SUCCESS.getHttpStatus())
                .header(HttpHeaders.SET_COOKIE, cookieManager.clear().toString())
                .body(new ApiResponse<>(
                        true,
                        SuccessStatus.LOGOUT_SUCCESS.getCode(),
                        SuccessStatus.LOGOUT_SUCCESS.getMessage(),
                        null
                ));
    }
}
