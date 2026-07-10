package com.leets7th.job_is_be.domain.deck.entity;

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
 * 사용자별 일일 추천 덱 헤더
 */
@Entity
@Table(
        name = "decks",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_decks_user_date_slot",
                columnNames = {"user_id", "deck_date", "slot"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Deck extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "deck_date", nullable = false)
    private LocalDate deckDate;

    @Column(nullable = false, length = 10)
    private String slot; // 07:30, 12:30, 18:30 (SET-01)

    @Column(name = "empty_reason", length = 30)
    private String emptyReason; // no_candidates, onboarding_incomplete, pre_slot (REC-07)

    @Column(name = "first_opened_at")
    private LocalDateTime firstOpenedAt; // 재방문 판별 기준 (REC-06)

    @Column(name = "completed_at")
    private LocalDateTime completedAt; // 전량 처리 완료 시점 (REC-05)

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public Deck(User user, LocalDate deckDate, String slot, LocalDateTime createdAt) {
        this.user = user;
        this.deckDate = deckDate;
        this.slot = slot;
        this.createdAt = createdAt;
    }

    public void markFirstOpened(LocalDateTime now) {
        if (this.firstOpenedAt == null) {
            this.firstOpenedAt = now;
        }
    }

    public void complete(LocalDateTime now) {
        this.completedAt = now;
    }

    public void markEmpty(String reason) {
        this.emptyReason = reason;
    }
}
