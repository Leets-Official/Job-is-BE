package com.leets7th.job_is_be.domain.job.entity;


import com.leets7th.job_is_be.domain.job.enums.JobStatus;
import com.leets7th.job_is_be.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 채용 공고
 */
@Entity
@Table(name = "jobs", uniqueConstraints = @UniqueConstraint(name = "uk_jobs_source_external_id", columnNames = {"source", "external_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Job extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_category_id")
    private JobCategory jobCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "career_level", length = 30)
    private String careerLevel; // 신입, 신입-주니어, 경력무관 등 (외부 원문 표기 그대로)

    @Column(name = "employment_type", length = 30)
    private String employmentType; // 정규직 등

    @Column(name = "remote_available", nullable = false)
    private boolean remoteAvailable;

    @Column(name = "salary_disclosed", nullable = false)
    private boolean salaryDisclosed; // §11 연봉 원문 확인 문구 처리 기준

    @Column(length = 50)
    private String source; // 원티드 등 원문 출처

    @Column(name = "external_id")
    private Long externalId; // 원문 출처 내 공고 id. (source, externalId)로 크롤링 동기화 시 upsert 매칭

    @Column(name = "source_url", length = 500)
    private String sourceUrl;

    @Column(name = "posted_at")
    private LocalDateTime postedAt;

    @Column(name = "deadline_at")
    private LocalDateTime deadlineAt; // NULL이면 "상시" 표시 (§4.4)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JobStatus status;

    @Column(name = "editor_note", length = 1000)
    private String editorNote; // Editor's Note (DET-01)

    // 기획 schema.sql 에서 요구하는 사항

    @Column(columnDefinition = "TEXT")
    private String intro; // 팀/회사 소개

    @Column(name = "main_tasks", columnDefinition = "TEXT")
    private String mainTasks; // 담당업무

    @Column(columnDefinition = "TEXT")
    private String requirements; // 자격요건

    @Column(name = "preferred_points", columnDefinition = "TEXT")
    private String preferredPoints; // 우대사항

    @Column(columnDefinition = "TEXT")
    private String benefits; // 복지/문화

    @Column(name = "career_min")
    private Integer careerMin; // 경력 최소 연수

    @Column(name = "career_max")
    private Integer careerMax; // 경력 최대 연수

    @Column(name = "reward_total")
    private String rewardTotal; // 추천 보상금

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl; // 카드 썸네일 URL

    @Builder
    public Job(Company company, JobCategory jobCategory, Region region, String title,
               String careerLevel, String employmentType, boolean remoteAvailable,
               boolean salaryDisclosed, String source, String sourceUrl,
               LocalDateTime postedAt, LocalDateTime deadlineAt, String editorNote,
               Long externalId, String intro, String mainTasks, String requirements,
               String preferredPoints, String benefits, Integer careerMin, Integer careerMax, // <- 괄호() 안에 이 파라미터들이 반드시 있어야 합니다.
               String rewardTotal, String thumbnailUrl) {

        this.company = company;
        this.jobCategory = jobCategory;
        this.region = region;
        this.title = title;
        this.careerLevel = careerLevel;
        this.employmentType = employmentType;
        this.remoteAvailable = remoteAvailable;
        this.salaryDisclosed = salaryDisclosed;
        this.source = source;
        this.externalId = externalId;
        this.sourceUrl = sourceUrl;
        this.postedAt = postedAt;
        this.deadlineAt = deadlineAt;
        this.editorNote = editorNote;
        this.status = JobStatus.ACTIVE;
        this.intro = intro;
        this.mainTasks = mainTasks;
        this.requirements = requirements;
        this.preferredPoints = preferredPoints;
        this.benefits = benefits;
        this.careerMin = careerMin;
        this.careerMax = careerMax;
        this.rewardTotal = rewardTotal;
        this.thumbnailUrl = thumbnailUrl;
    }

    public void expire() {
        this.status = JobStatus.EXPIRED;
    }

    public void markRemoved() {
        this.status = JobStatus.REMOVED;
    }

    public boolean isOpenEnded() {
        return this.deadlineAt == null;
    }

    // 크롤링 재수집 시 (source, externalId)로 매칭된 기존 공고에 최신 원문 내용을 반영
    public void syncFrom(Company company, String title, String careerLevel, String employmentType,
                          boolean remoteAvailable, String sourceUrl,
                          LocalDateTime postedAt, LocalDateTime deadlineAt, JobStatus status) {
        this.company = company;
        this.title = title;
        this.careerLevel = careerLevel;
        this.employmentType = employmentType;
        this.remoteAvailable = remoteAvailable;
        this.sourceUrl = sourceUrl;
        this.postedAt = postedAt;
        this.deadlineAt = deadlineAt;
        this.status = status;
    }
}
