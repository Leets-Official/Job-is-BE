package com.leets7th.job_is_be.domain.notification.entity;


import com.leets7th.job_is_be.domain.user.entity.User;
import com.leets7th.job_is_be.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 알림/발송 수신 설정
 */
@Entity
@Table(
        name = "notification_settings",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_notification_settings_user",
                columnNames = "user_id"
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationSetting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "email_subscribed", nullable = false)
    private boolean emailSubscribed;

    @Column(name = "marketing_subscribed", nullable = false)
    private boolean marketingSubscribed;

    @Column(name = "send_slot", nullable = false, length = 10)
    private String sendSlot; // 07:30, 12:30, 18:30

    @Column(name = "snooze_until")
    private LocalDate snoozeUntil; // 스누즈 재개 예정일 (무기한 스누즈면 null)

    @Column(name = "snooze_indefinite", nullable = false)
    private boolean snoozeIndefinite;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Builder
    public NotificationSetting(User user, String sendSlot) {
        this.user = user;
        this.sendSlot = sendSlot;
        this.emailSubscribed = true;
        this.marketingSubscribed = false;
        this.emailVerified = false;
    }

    public void changeSlot(String slot) {
        this.sendSlot = slot;
    }

    public void updateEmailSubscribed(boolean emailSubscribed) {
        this.emailSubscribed = emailSubscribed;
    }

    public void updateMarketingSubscribed(boolean marketingSubscribed) {
        this.marketingSubscribed = marketingSubscribed;
    }

    public void snooze(LocalDate until) {
        this.snoozeUntil = until;
        this.snoozeIndefinite = false;
    }

    public void snoozeIndefinitely() {
        this.snoozeUntil = null;
        this.snoozeIndefinite = true;
    }

    public void clearSnooze() {
        this.snoozeUntil = null;
        this.snoozeIndefinite = false;
    }

    public void resetEmailVerification() {
        this.emailVerified = false;
    }
}
