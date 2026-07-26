package com.leets7th.job_is_be.domain.auth.service;

import com.leets7th.job_is_be.domain.auth.dto.ConsentRequest;
import com.leets7th.job_is_be.domain.user.entity.User;
import com.leets7th.job_is_be.domain.user.entity.UserConsent;
import com.leets7th.job_is_be.domain.user.repository.UserConsentRepository;
import com.leets7th.job_is_be.domain.user.repository.UserRepository;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AccountService {

    private final UserRepository userRepository;
    private final UserConsentRepository userConsentRepository;

    public AccountService(
            UserRepository userRepository,
            UserConsentRepository userConsentRepository
    ) {
        this.userRepository = userRepository;
        this.userConsentRepository = userConsentRepository;
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
}
