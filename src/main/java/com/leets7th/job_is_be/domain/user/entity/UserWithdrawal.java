package com.leets7th.job_is_be.domain.user.entity;

import com.leets7th.job_is_be.domain.user.enums.WithdrawalStatus;
import com.leets7th.job_is_be.domain.user.enums.WithdrawalReasonCode;
import com.leets7th.job_is_be.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 회원 탈퇴 요청 및 30일 복구 유예
 */
@Entity
@Table(name = "user_withdrawals")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserWithdrawal extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason_code", length = 30)
    private WithdrawalReasonCode reasonCode;

    @Column(name = "reason_detail", length = 500)
    private String reasonDetail;

    // TODO: LocalDateTime → OffsetDateTime으로 통일 필요 (BaseEntity와 타입 불일치)
    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    // TODO: LocalDateTime → OffsetDateTime으로 통일 필요 (BaseEntity와 타입 불일치)
    @Column(name = "scheduled_deletion_at")
    private LocalDateTime scheduledDeletionAt; // 신청일 + 30일

    // TODO: LocalDateTime → OffsetDateTime으로 통일 필요 (BaseEntity와 타입 불일치)
    @Column(name = "restored_at")
    private LocalDateTime restoredAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WithdrawalStatus status;

    @Builder
    public UserWithdrawal(
            User user,
            WithdrawalReasonCode reasonCode,
            String reasonDetail,
            LocalDateTime requestedAt,
            LocalDateTime scheduledDeletionAt
    ) {
        this.user = user;
        this.reasonCode = reasonCode;
        this.reasonDetail = reasonDetail;
        this.requestedAt = requestedAt;
        this.scheduledDeletionAt = scheduledDeletionAt;
        this.status = WithdrawalStatus.PENDING;
    }

    public void restore(LocalDateTime now) {
        this.status = WithdrawalStatus.RESTORED;
        this.restoredAt = now;
    }

    public void complete() {
        this.status = WithdrawalStatus.COMPLETED;
    }
}
