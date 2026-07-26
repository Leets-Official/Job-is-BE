package com.leets7th.job_is_be.domain.user.service;

import com.leets7th.job_is_be.domain.notification.entity.NotificationSetting;
import com.leets7th.job_is_be.domain.notification.repository.NotificationSettingRepository;
import com.leets7th.job_is_be.domain.user.dto.AccountResponse;
import com.leets7th.job_is_be.domain.user.entity.User;
import com.leets7th.job_is_be.domain.user.enums.SocialType;
import com.leets7th.job_is_be.domain.user.repository.UserRepository;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationSettingRepository notificationSettingRepository;

    @InjectMocks
    private AccountService accountService;

    private User user() {
        User user = User.builder().socialId("s").socialType(SocialType.KAKAO).email("a@a.com").build();
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    @Test
    void 사용자가_없으면_예외를_던진다() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        GeneralException exception = catchThrowableOfType(
                () -> accountService.getAccount(1L), GeneralException.class);

        assertThat(exception.getErrorStatus()).isEqualTo(ErrorStatus.USER_NOT_FOUND);
    }

    @Test
    void 계정_정보를_조회한다() {
        User user = user();
        NotificationSetting setting = NotificationSetting.builder().user(user).sendSlot("07:30").build();
        ReflectionTestUtils.setField(setting, "emailVerified", true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationSettingRepository.findByUser(user)).thenReturn(Optional.of(setting));

        AccountResponse response = accountService.getAccount(1L);

        assertThat(response.socialType()).isEqualTo(SocialType.KAKAO);
        assertThat(response.receivingEmail()).isEqualTo("a@a.com");
        assertThat(response.emailVerified()).isTrue();
    }

    @Test
    void 알림_설정이_없으면_이메일_미확인_상태로_반환한다() {
        User user = user();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationSettingRepository.findByUser(user)).thenReturn(Optional.empty());

        AccountResponse response = accountService.getAccount(1L);

        assertThat(response.emailVerified()).isFalse();
    }
}
