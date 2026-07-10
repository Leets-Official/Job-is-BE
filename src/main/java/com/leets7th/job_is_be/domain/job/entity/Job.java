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
@Table(name = "jobs")
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

    @Builder
    public Job(Company company, JobCategory jobCategory, Region region, String title,
               String careerLevel, String employmentType, boolean remoteAvailable,
               boolean salaryDisclosed, String source, String sourceUrl,
               LocalDateTime postedAt, LocalDateTime deadlineAt, String editorNote) {
        this.company = company;
        this.jobCategory = jobCategory;
        this.region = region;
        this.title = title;
        this.careerLevel = careerLevel;
        this.employmentType = employmentType;
        this.remoteAvailable = remoteAvailable;
        this.salaryDisclosed = salaryDisclosed;
        this.source = source;
        this.sourceUrl = sourceUrl;
        this.postedAt = postedAt;
        this.deadlineAt = deadlineAt;
        this.editorNote = editorNote;
        this.status = JobStatus.ACTIVE;
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
}
