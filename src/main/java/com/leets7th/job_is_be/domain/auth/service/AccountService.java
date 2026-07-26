package com.leets7th.job_is_be.domain.auth.service;

import com.leets7th.job_is_be.domain.auth.dto.ConsentRequest;
import com.leets7th.job_is_be.domain.auth.dto.WithdrawalRequest;
import com.leets7th.job_is_be.domain.auth.dto.WithdrawalResponse;
import com.leets7th.job_is_be.domain.user.entity.User;
import com.leets7th.job_is_be.domain.user.entity.UserConsent;
import com.leets7th.job_is_be.domain.user.entity.UserWithdrawal;
import com.leets7th.job_is_be.domain.user.enums.UserStatus;
import com.leets7th.job_is_be.domain.user.enums.WithdrawalStatus;
import com.leets7th.job_is_be.domain.user.repository.UserConsentRepository;
import com.leets7th.job_is_be.domain.user.repository.UserRepository;
import com.leets7th.job_is_be.domain.user.repository.UserWithdrawalRepository;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AccountService {

    private final UserRepository userRepository;
    private final UserConsentRepository userConsentRepository;
    private final UserWithdrawalRepository userWithdrawalRepository;
    private final RefreshTokenSessionStore refreshTokenSessionStore;

    public AccountService(
            UserRepository userRepository,
            UserConsentRepository userConsentRepository,
            UserWithdrawalRepository userWithdrawalRepository,
            RefreshTokenSessionStore refreshTokenSessionStore
    ) {
        this.userRepository = userRepository;
        this.userConsentRepository = userConsentRepository;
        this.userWithdrawalRepository = userWithdrawalRepository;
        this.refreshTokenSessionStore = refreshTokenSessionStore;
    }

    @Transactional
    public void saveConsent(Long userId, ConsentRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
        LocalDateTime now = LocalDateTime.now();
        boolean marketingAgreed = Boolean.TRUE.equals(request.marketingAgreed());

        UserConsent consent = userConsentRepository.findByUserId(userId)
                .orElseGet(() -> UserConsent.builder()
                        .user(user)
                        .termsAgreed(true)
                        .privacyAgreed(true)
                        .ageOver14Agreed(true)
                        .marketingAgreed(marketingAgreed)
                        .agreedAt(now)
                        .build());
        consent.update(true, true, true, marketingAgreed, now);
        userConsentRepository.save(consent);
    }

    @Transactional
    public WithdrawalResponse withdraw(Long userId, WithdrawalRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
        if (user.getStatus() == UserStatus.WITHDRAWN
                || userWithdrawalRepository
                .findFirstByUserIdAndStatusOrderByRequestedAtDesc(userId, WithdrawalStatus.PENDING)
                .isPresent()) {
            throw new GeneralException(ErrorStatus.WITHDRAWAL_ALREADY_REQUESTED);
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime restorableUntil = now.plusDays(30);
        WithdrawalRequest safeRequest = request == null
                ? new WithdrawalRequest(null, null)
                : request;

        user.withdraw(now);
        userWithdrawalRepository.save(UserWithdrawal.builder()
                .user(user)
                .reasonCode(safeRequest.reasonCode())
                .reasonDetail(normalize(safeRequest.reasonDetail()))
                .requestedAt(now)
                .scheduledDeletionAt(restorableUntil)
                .build());
        userRepository.flush();
        userWithdrawalRepository.flush();
        refreshTokenSessionStore.revokeAll(userId);

        return new WithdrawalResponse(restorableUntil);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
