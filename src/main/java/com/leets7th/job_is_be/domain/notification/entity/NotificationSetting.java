package com.leets7th.job_is_be.domain.notification.entity;


import com.leets7th.job_is_be.domain.user.entity.User;
import com.leets7th.job_is_be.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @Column(name = "send_slot", nullable = false, length = 10)
    private String sendSlot; // 07:30, 12:30, 18:30

    @Column(name = "snooze_until")
    private LocalDate snoozeUntil; // 스누즈 재개 예정일

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public NotificationSetting(User user, String sendSlot) {
        this.user = user;
        this.sendSlot = sendSlot;
        this.emailSubscribed = true;
        this.emailVerified = false;
    }

    public void changeSlot(String slot, LocalDateTime now) {
        this.sendSlot = slot;
        this.updatedAt = now;
    }

    public void snooze(LocalDate until, LocalDateTime now) {
        this.snoozeUntil = until;
        this.updatedAt = now;
    }

    public void unsubscribe(LocalDateTime now) {
        this.emailSubscribed = false;
        this.updatedAt = now;
    }

    public void resetEmailVerification() {
        this.emailVerified = false;
    }
}

