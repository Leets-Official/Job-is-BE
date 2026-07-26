package com.leets7th.job_is_be.domain.auth.controller;

import com.leets7th.job_is_be.domain.auth.dto.ConsentRequest;
import com.leets7th.job_is_be.domain.auth.dto.OAuthExchangeResponse;
import com.leets7th.job_is_be.domain.auth.dto.RestoreRequest;
import com.leets7th.job_is_be.domain.auth.dto.WithdrawalRequest;
import com.leets7th.job_is_be.domain.auth.dto.WithdrawalResponse;
import com.leets7th.job_is_be.domain.auth.service.AccountService;
import com.leets7th.job_is_be.domain.auth.service.AccountRecoveryService;
import com.leets7th.job_is_be.domain.auth.service.RefreshTokenCookieManager;
import com.leets7th.job_is_be.global.response.ApiResponse;
import com.leets7th.job_is_be.global.status.SuccessStatus;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AccountController {

    private final AccountService accountService;
    private final AccountRecoveryService accountRecoveryService;
    private final RefreshTokenCookieManager cookieManager;

    public AccountController(
            AccountService accountService,
            AccountRecoveryService accountRecoveryService,
            RefreshTokenCookieManager cookieManager
    ) {
        this.accountService = accountService;
        this.accountRecoveryService = accountRecoveryService;
        this.cookieManager = cookieManager;
    }

    @PostMapping("/consent")
    public ResponseEntity<ApiResponse<Void>> consent(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ConsentRequest request
    ) {
        accountService.saveConsent(Long.valueOf(jwt.getSubject()), request);
        return ApiResponse.success(SuccessStatus.CONSENT_SAVE_SUCCESS);
    }

    @DeleteMapping("/withdraw")
    public ResponseEntity<ApiResponse<WithdrawalResponse>> withdraw(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody(required = false) WithdrawalRequest request
    ) {
        WithdrawalResponse response = accountService.withdraw(Long.valueOf(jwt.getSubject()), request);
        return ResponseEntity
                .status(SuccessStatus.WITHDRAWAL_SUCCESS.getHttpStatus())
                .header(HttpHeaders.SET_COOKIE, cookieManager.clear().toString())
                .body(new ApiResponse<>(
                        true,
                        SuccessStatus.WITHDRAWAL_SUCCESS.getCode(),
                        SuccessStatus.WITHDRAWAL_SUCCESS.getMessage(),
                        response
                ));
    }

    @PostMapping("/restore")
    public ResponseEntity<ApiResponse<OAuthExchangeResponse>> restore(
            @Valid @RequestBody RestoreRequest request
    ) {
        AccountRecoveryService.RestoreResult result =
                accountRecoveryService.restore(request.restoreCode());
        return ResponseEntity
                .status(SuccessStatus.WITHDRAWAL_RESTORE_SUCCESS.getHttpStatus())
                .header(HttpHeaders.SET_COOKIE, cookieManager.create(result.refreshToken()).toString())
                .body(new ApiResponse<>(
                        true,
                        SuccessStatus.WITHDRAWAL_RESTORE_SUCCESS.getCode(),
                        SuccessStatus.WITHDRAWAL_RESTORE_SUCCESS.getMessage(),
                        result.response()
                ));
    }
}
