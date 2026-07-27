package com.leets7th.job_is_be.domain.auth.service;

import com.leets7th.job_is_be.domain.auth.dto.WithdrawalRequest;
import com.leets7th.job_is_be.domain.user.entity.User;
import com.leets7th.job_is_be.domain.user.entity.UserWithdrawal;
import com.leets7th.job_is_be.domain.user.enums.SocialType;
import com.leets7th.job_is_be.domain.user.enums.UserStatus;
import com.leets7th.job_is_be.domain.user.enums.WithdrawalReasonCode;
import com.leets7th.job_is_be.domain.user.enums.WithdrawalStatus;
import com.leets7th.job_is_be.domain.user.repository.UserConsentRepository;
import com.leets7th.job_is_be.domain.user.repository.UserRepository;
import com.leets7th.job_is_be.domain.user.repository.UserWithdrawalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserConsentRepository userConsentRepository;
    @Mock
    private UserWithdrawalRepository userWithdrawalRepository;
    @Mock
    private RefreshTokenSessionStore refreshTokenSessionStore;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(
                userRepository,
                userConsentRepository,
                userWithdrawalRepository,
                refreshTokenSessionStore
        );
    }

    @Test
    void withdrawsForThirtyDaysAndRevokesEveryRefreshSession() {
        User user = User.builder()
                .socialId("social-id")
                .socialType(SocialType.KAKAO)
                .email("user@example.com")
                .build();
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user));
        when(userWithdrawalRepository.findFirstByUserIdAndStatusOrderByRequestedAtDesc(
                1L,
                WithdrawalStatus.PENDING
        )).thenReturn(Optional.empty());
        LocalDateTime before = LocalDateTime.now().plusDays(30).minusSeconds(1);

        var response = accountService.withdraw(
                1L,
                new WithdrawalRequest(WithdrawalReasonCode.OTHER, "  직접 입력 사유  ")
        );

        LocalDateTime after = LocalDateTime.now().plusDays(30).plusSeconds(1);
        assertThat(user.getStatus()).isEqualTo(UserStatus.WITHDRAWN);
        assertThat(response.restorableUntil()).isBetween(before, after);

        ArgumentCaptor<UserWithdrawal> withdrawalCaptor =
                ArgumentCaptor.forClass(UserWithdrawal.class);
        verify(userWithdrawalRepository).save(withdrawalCaptor.capture());
        UserWithdrawal withdrawal = withdrawalCaptor.getValue();
        assertThat(withdrawal.getReasonCode()).isEqualTo(WithdrawalReasonCode.OTHER);
        assertThat(withdrawal.getReasonDetail()).isEqualTo("직접 입력 사유");
        assertThat(withdrawal.getStatus()).isEqualTo(WithdrawalStatus.PENDING);
        assertThat(withdrawal.getScheduledDeletionAt()).isEqualTo(response.restorableUntil());
        verify(refreshTokenSessionStore).revokeAll(1L);
    }
}
