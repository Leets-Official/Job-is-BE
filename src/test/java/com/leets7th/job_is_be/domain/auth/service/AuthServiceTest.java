package com.leets7th.job_is_be.domain.auth.service;

import com.leets7th.job_is_be.domain.user.repository.UserProfileRepository;
import com.leets7th.job_is_be.domain.user.repository.UserRepository;
import com.leets7th.job_is_be.domain.user.repository.UserWithdrawalRepository;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.jwt.JwtTokenProvider;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JwtTokenProvider tokenProvider;
    @Mock
    private RefreshTokenSessionStore sessionStore;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserProfileRepository userProfileRepository;
    @Mock
    private UserWithdrawalRepository userWithdrawalRepository;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                tokenProvider,
                sessionStore,
                userRepository,
                userProfileRepository,
                userWithdrawalRepository
        );
    }

    @Test
    void rotatesRefreshTokenWhenReissuing() {
        String oldRefreshToken = "old-refresh-token";
        JwtTokenProvider.RefreshTokenClaims claims =
                new JwtTokenProvider.RefreshTokenClaims(1L, "old-session");
        JwtTokenProvider.TokenPair newPair = new JwtTokenProvider.TokenPair(
                "new-access-token",
                "new-refresh-token",
                "new-session",
                900,
                Duration.ofDays(14)
        );

        when(tokenProvider.decodeRefreshToken(oldRefreshToken)).thenReturn(claims);
        when(sessionStore.consume("old-session", 1L, oldRefreshToken)).thenReturn(true);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(tokenProvider.issueTokenPair(1L)).thenReturn(newPair);

        AuthService.ReissueResult result = authService.reissue(oldRefreshToken);

        assertEquals("new-access-token", result.response().accessToken());
        assertEquals("new-refresh-token", result.refreshToken());
        verify(sessionStore).save("new-session", 1L, "new-refresh-token", Duration.ofDays(14));
    }

    @Test
    void rejectsAlreadyConsumedRefreshToken() {
        String refreshToken = "refresh-token";
        when(tokenProvider.decodeRefreshToken(refreshToken))
                .thenReturn(new JwtTokenProvider.RefreshTokenClaims(1L, "session"));
        when(sessionStore.consume("session", 1L, refreshToken)).thenReturn(false);

        GeneralException exception = assertThrows(
                GeneralException.class,
                () -> authService.reissue(refreshToken)
        );

        assertEquals(ErrorStatus.REFRESH_SESSION_NOT_FOUND, exception.getErrorStatus());
    }

    @Test
    void logoutWithoutCookieIsIdempotent() {
        authService.logout(null);
    }
}
