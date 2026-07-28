package com.leets7th.job_is_be.domain.user.entity;


import com.leets7th.job_is_be.domain.user.enums.CareerLevel;
import com.leets7th.job_is_be.domain.user.enums.OnboardingStep;
import com.leets7th.job_is_be.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 사용자 프로필. User와 1:1 관계
 */
@Entity
@Table(name = "user_profiles", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_profiles_user", columnNames = "user_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "career_level", length = 20)
    private CareerLevel careerLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "onboarding_step", length = 20)
    private OnboardingStep onboardingStep;

    @Column(name = "preference_note", length = 500)
    private String preferenceNote; // 선호 조건 텍스트 (재택 우선 등)

    @Column(name = "exclude_keywords", length = 500)
    private String excludeKeywords; // 제외 조건 (야근 없는 곳 등)

    @Column(name = "tech_stack", length = 500)
    private String techStack; // 보유/선호 기술 스택, comma-separated

    @Column(name = "personality_tags", length = 500)
    private String personalityTags;

    @Column(name = "is_job_test_completed", nullable = false)
    private boolean jobTestCompleted;

    // TODO: LocalDateTime → OffsetDateTime으로 통일 필요 (BaseEntity와 타입 불일치)
    @Column(name = "job_test_completed_at")
    private LocalDateTime jobTestCompletedAt;

    @Column(name = "onboarding_completed", nullable = false)
    private boolean onboardingCompleted;

    // TODO: LocalDateTime → OffsetDateTime으로 통일 필요 (BaseEntity와 타입 불일치)
    @Column(name = "onboarding_completed_at")
    private LocalDateTime onboardingCompletedAt;

    @Builder
    public UserProfile(User user, CareerLevel careerLevel, OnboardingStep onboardingStep,
                       String preferenceNote, String excludeKeywords, String techStack) {
        this.user = user;
        this.careerLevel = careerLevel;
        this.onboardingStep = onboardingStep;
        this.preferenceNote = preferenceNote;
        this.excludeKeywords = excludeKeywords;
        this.techStack = techStack;
        this.jobTestCompleted = false;
        this.onboardingCompleted = false;
    }

    public void completeOnboarding(LocalDateTime now) {
        this.onboardingCompleted = true;
        this.onboardingCompletedAt = now;
    }

    public void updateDraft(
            CareerLevel careerLevel,
            OnboardingStep onboardingStep,
            String preferenceNote,
            String excludeKeywords,
            String techStack
    ) {
        this.careerLevel = careerLevel;
        this.onboardingStep = onboardingStep;
        this.preferenceNote = preferenceNote;
        this.excludeKeywords = excludeKeywords;
        this.techStack = techStack;
    }

    public void updateProfile(
            CareerLevel careerLevel,
            String preferenceNote,
            String excludeKeywords,
            String techStack
    ) {
        this.careerLevel = careerLevel;
        this.preferenceNote = preferenceNote;
        this.excludeKeywords = excludeKeywords;
        this.techStack = techStack;
    }

    public void moveOnboardingStep(OnboardingStep onboardingStep) {
        this.onboardingStep = onboardingStep;
    }

    public void completeJobTest(LocalDateTime now) {
        this.jobTestCompleted = true;
        this.jobTestCompletedAt = now;
    }

    public void applyPersonalityTags(String personalityTags, LocalDateTime now) {
        this.personalityTags = personalityTags;
        completeJobTest(now);
    }
}
