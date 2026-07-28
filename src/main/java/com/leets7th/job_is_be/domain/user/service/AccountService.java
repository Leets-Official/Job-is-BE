package com.leets7th.job_is_be.domain.user.service;

import com.leets7th.job_is_be.domain.notification.entity.NotificationSetting;
import com.leets7th.job_is_be.domain.notification.repository.NotificationSettingRepository;
import com.leets7th.job_is_be.domain.user.dto.AccountResponse;
import com.leets7th.job_is_be.domain.user.entity.User;
import com.leets7th.job_is_be.domain.user.repository.UserRepository;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountService {

    private final UserRepository userRepository;
    private final NotificationSettingRepository notificationSettingRepository;

    public AccountResponse getAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        boolean emailVerified = notificationSettingRepository.findByUser(user)
                .map(NotificationSetting::isEmailVerified)
                .orElse(false);

        return new AccountResponse(
                user.getSocialType(),
                user.getCreatedAt(),
                user.getEmail(),
                emailVerified
        );
    }
}
