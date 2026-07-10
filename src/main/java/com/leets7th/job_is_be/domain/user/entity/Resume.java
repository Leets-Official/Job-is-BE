package com.leets7th.job_is_be.domain.user.entity;

import com.leets7th.job_is_be.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 이력서/자소서 파일
 */
@Entity
@Table(name = "resumes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Resume extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_type", nullable = false, length = 10)
    private String fileType; // PDF, DOCX, HWP, HWPX

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public Resume(User user, String fileName, String fileType, String fileUrl, LocalDateTime uploadedAt) {
        this.user = user;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileUrl = fileUrl;
        this.uploadedAt = uploadedAt;
        this.active = true;
    }

    public void delete(LocalDateTime now) {
        this.active = false;
        this.deletedAt = now;
    }
}
