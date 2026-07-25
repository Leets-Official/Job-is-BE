package com.leets7th.job_is_be.domain.notification.service;

import com.leets7th.job_is_be.domain.notification.dto.NotificationSettingResponse;
import com.leets7th.job_is_be.domain.notification.dto.NotificationSettingUpdateRequest;
import com.leets7th.job_is_be.domain.notification.dto.SnoozeRequest;
import com.leets7th.job_is_be.domain.notification.entity.NotificationSetting;
import com.leets7th.job_is_be.domain.notification.enums.SnoozeDuration;
import com.leets7th.job_is_be.domain.notification.repository.NotificationSettingRepository;
import com.leets7th.job_is_be.domain.user.entity.User;
import com.leets7th.job_is_be.domain.user.enums.SocialType;
import com.leets7th.job_is_be.domain.user.repository.UserRepository;
import com.leets7th.job_is_be.global.exception.GeneralException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationSettingServiceTest {

    @Mock
    private NotificationSettingRepository notificationSettingRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationSettingService notificationSettingService;

    private User user() {
        User user = User.builder().socialId("s").socialType(SocialType.KAKAO).email("a@a.com").build();
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    @Test
    void 사용자가_없으면_예외를_던진다() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationSettingService.getSetting(1L))
                .isInstanceOf(GeneralException.class);
    }

    @Test
    void 설정이_없으면_기본값으로_생성해서_조회한다() {
        User user = user();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationSettingRepository.findByUser(user)).thenReturn(Optional.empty());
        when(notificationSettingRepository.save(any(NotificationSetting.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NotificationSettingResponse response = notificationSettingService.getSetting(1L);

        assertThat(response.sendSlot()).isEqualTo("07:30");
        assertThat(response.briefingEnabled()).isTrue();
        assertThat(response.marketingSubscribed()).isFalse();
        assertThat(response.snooze().snoozed()).isFalse();
    }

    @Test
    void 허용되지_않은_슬롯으로_변경하면_예외를_던진다() {
        User user = user();
        NotificationSetting setting = NotificationSetting.builder().user(user).sendSlot("07:30").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationSettingRepository.findByUser(user)).thenReturn(Optional.of(setting));

        NotificationSettingUpdateRequest request = new NotificationSettingUpdateRequest(null, "09:00", null);

        assertThatThrownBy(() -> notificationSettingService.updateSetting(1L, request))
                .isInstanceOf(GeneralException.class);
    }

    @Test
    void 수신_설정을_변경한다() {
        User user = user();
        NotificationSetting setting = NotificationSetting.builder().user(user).sendSlot("07:30").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationSettingRepository.findByUser(user)).thenReturn(Optional.of(setting));

        NotificationSettingUpdateRequest request = new NotificationSettingUpdateRequest(false, "12:30", true);
        NotificationSettingResponse response = notificationSettingService.updateSetting(1L, request);

        assertThat(response.briefingEnabled()).isFalse();
        assertThat(response.sendSlot()).isEqualTo("12:30");
        assertThat(response.marketingSubscribed()).isTrue();
    }

    @Test
    void 스누즈를_7일_설정한다() {
        User user = user();
        NotificationSetting setting = NotificationSetting.builder().user(user).sendSlot("07:30").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationSettingRepository.findByUser(user)).thenReturn(Optional.of(setting));

        notificationSettingService.snooze(1L, new SnoozeRequest(SnoozeDuration.SEVEN_DAYS));

        assertThat(setting.getSnoozeUntil()).isEqualTo(LocalDate.now().plusDays(7));
        assertThat(setting.isSnoozeIndefinite()).isFalse();
    }

    @Test
    void 스누즈를_무기한_설정한다() {
        User user = user();
        NotificationSetting setting = NotificationSetting.builder().user(user).sendSlot("07:30").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationSettingRepository.findByUser(user)).thenReturn(Optional.of(setting));

        notificationSettingService.snooze(1L, new SnoozeRequest(SnoozeDuration.INDEFINITE));

        assertThat(setting.isSnoozeIndefinite()).isTrue();
        assertThat(setting.getSnoozeUntil()).isNull();
    }

    @Test
    void 재요청시_기존_스누즈_값을_덮어쓴다() {
        User user = user();
        NotificationSetting setting = NotificationSetting.builder().user(user).sendSlot("07:30").build();
        setting.snoozeIndefinitely();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationSettingRepository.findByUser(user)).thenReturn(Optional.of(setting));

        notificationSettingService.snooze(1L, new SnoozeRequest(SnoozeDuration.THIRTY_DAYS));

        assertThat(setting.isSnoozeIndefinite()).isFalse();
        assertThat(setting.getSnoozeUntil()).isEqualTo(LocalDate.now().plusDays(30));
    }

    @Test
    void 스누즈를_해제하면_즉시_재개된다() {
        User user = user();
        NotificationSetting setting = NotificationSetting.builder().user(user).sendSlot("07:30").build();
        setting.snooze(LocalDate.now().plusDays(7));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationSettingRepository.findByUser(user)).thenReturn(Optional.of(setting));

        notificationSettingService.cancelSnooze(1L);

        assertThat(setting.getSnoozeUntil()).isNull();
        assertThat(setting.isSnoozeIndefinite()).isFalse();
    }
}
