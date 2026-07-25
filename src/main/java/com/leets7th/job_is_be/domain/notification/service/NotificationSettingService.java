package com.leets7th.job_is_be.domain.notification.service;

import com.leets7th.job_is_be.domain.notification.dto.NotificationSettingResponse;
import com.leets7th.job_is_be.domain.notification.entity.NotificationSetting;
import com.leets7th.job_is_be.domain.notification.repository.NotificationSettingRepository;
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
public class NotificationSettingService {

    private static final String DEFAULT_SEND_SLOT = "07:30";

    private final NotificationSettingRepository notificationSettingRepository;
    private final UserRepository userRepository;

    @Transactional
    public NotificationSettingResponse getSetting(Long userId) {
        return toResponse(getOrCreateSetting(userId));
    }

    private NotificationSetting getOrCreateSetting(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
        return notificationSettingRepository.findByUser(user)
                .orElseGet(() -> notificationSettingRepository.save(
                        NotificationSetting.builder()
                                .user(user)
                                .sendSlot(DEFAULT_SEND_SLOT)
                                .build()
                ));
    }

    private NotificationSettingResponse toResponse(NotificationSetting setting) {
        NotificationSettingResponse.SnoozeInfo snoozeInfo = new NotificationSettingResponse.SnoozeInfo(
                setting.isSnoozeIndefinite() || setting.getSnoozeUntil() != null,
                setting.getSnoozeUntil(),
                setting.isSnoozeIndefinite()
        );
        return new NotificationSettingResponse(
                setting.isEmailSubscribed(),
                setting.getSendSlot(),
                setting.isMarketingSubscribed(),
                snoozeInfo
        );
    }
}
