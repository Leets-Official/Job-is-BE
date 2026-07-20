package com.leets7th.job_is_be.domain.auth.service;

import com.leets7th.job_is_be.domain.auth.dto.DevLoginResponse;
import com.leets7th.job_is_be.domain.user.repository.UserRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DevAuthServiceTest {

    @Mock
    private JwtTokenProvider tokenProvider;
    @Mock
    private RefreshTokenSessionStore sessionStore;
    @Mock
    private UserRepository userRepository;

    private DevAuthService devAuthService;

    @BeforeEach
    void setUp() {
        devAuthService = new DevAuthService(tokenProvider, sessionStore, userRepository);
    }

    @Test
    void issuesTokenPairForExistingUser() {
        JwtTokenProvider.TokenPair tokenPair = new JwtTokenProvider.TokenPair(
                "access-token",
                "refresh-token",
                "refresh-session",
                900,
                Duration.ofDays(14)
        );
        when(userRepository.existsById(1L)).thenReturn(true);
        when(tokenProvider.issueTokenPair(1L)).thenReturn(tokenPair);

        DevLoginResponse response = devAuthService.login(1L);

        assertEquals(1L, response.userId());
        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(900, response.expiresIn());
        verify(sessionStore).save(
                "refresh-session",
                1L,
                "refresh-token",
                Duration.ofDays(14)
        );
    }

    @Test
    void rejectsMissingUserId() {
        GeneralException exception = assertThrows(
                GeneralException.class,
                () -> devAuthService.login(null)
        );

        assertEquals(ErrorStatus.DEV_LOGIN_USER_ID_REQUIRED, exception.getErrorStatus());
        verify(tokenProvider, never()).issueTokenPair(org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void rejectsUnknownUser() {
        when(userRepository.existsById(99L)).thenReturn(false);

        GeneralException exception = assertThrows(
                GeneralException.class,
                () -> devAuthService.login(99L)
        );

        assertEquals(ErrorStatus.USER_NOT_FOUND, exception.getErrorStatus());
        verify(tokenProvider, never()).issueTokenPair(org.mockito.ArgumentMatchers.anyLong());
    }
}
