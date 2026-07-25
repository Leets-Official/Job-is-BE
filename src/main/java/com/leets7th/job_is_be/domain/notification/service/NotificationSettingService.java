package com.leets7th.job_is_be.domain.notification.service;

import com.leets7th.job_is_be.domain.notification.dto.NotificationSettingResponse;
import com.leets7th.job_is_be.domain.notification.dto.NotificationSettingUpdateRequest;
import com.leets7th.job_is_be.domain.notification.dto.SnoozeRequest;
import com.leets7th.job_is_be.domain.notification.entity.NotificationSetting;
import com.leets7th.job_is_be.domain.notification.repository.NotificationSettingRepository;
import com.leets7th.job_is_be.domain.user.entity.User;
import com.leets7th.job_is_be.domain.user.repository.UserRepository;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationSettingService {

    private static final String DEFAULT_SEND_SLOT = "07:30";
    private static final Set<String> ALLOWED_SEND_SLOTS = Set.of("07:30", "12:30", "18:30");

    private final NotificationSettingRepository notificationSettingRepository;
    private final UserRepository userRepository;

    @Transactional
    public NotificationSettingResponse getSetting(Long userId) {
        return toResponse(getOrCreateSetting(userId));
    }

    @Transactional
    public NotificationSettingResponse updateSetting(Long userId, NotificationSettingUpdateRequest request) {
        NotificationSetting setting = getOrCreateSetting(userId);

        if (request.sendSlot() != null) {
            validateSendSlot(request.sendSlot());
            setting.changeSlot(request.sendSlot());
        }
        if (request.briefingEnabled() != null) {
            setting.updateEmailSubscribed(request.briefingEnabled());
        }
        if (request.marketingSubscribed() != null) {
            setting.updateMarketingSubscribed(request.marketingSubscribed());
        }

        return toResponse(setting);
    }

    @Transactional
    public void snooze(Long userId, SnoozeRequest request) {
        NotificationSetting setting = getOrCreateSetting(userId);
        switch (request.duration()) {
            case SEVEN_DAYS -> setting.snooze(LocalDate.now().plusDays(7));
            case THIRTY_DAYS -> setting.snooze(LocalDate.now().plusDays(30));
            case INDEFINITE -> setting.snoozeIndefinitely();
        }
    }

    private void validateSendSlot(String sendSlot) {
        if (!ALLOWED_SEND_SLOTS.contains(sendSlot)) {
            throw new GeneralException(ErrorStatus.NOTIFICATION_INVALID_SEND_SLOT);
        }
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
