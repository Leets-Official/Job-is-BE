package com.leets7th.job_is_be.domain.notification.repository;

import com.leets7th.job_is_be.domain.notification.entity.NotificationSetting;
import com.leets7th.job_is_be.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {

    Optional<NotificationSetting> findByUser(User user);

    Optional<NotificationSetting> findByUnsubscribeToken(String unsubscribeToken);
}
