package com.leets7th.job_is_be.domain.job.entity;

import com.leets7th.job_is_be.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 기업 정보
@Entity
@Table(name = "companies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Company extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(length = 50)
    private String source; // 데이터 출처 (원티드 등)

    @Builder
    public Company(String name, String description, String logoUrl, String source) {
        this.name = name;
        this.description = description;
        this.logoUrl = logoUrl;
        this.source = source;
    }
}
