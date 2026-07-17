package com.leets7th.job_is_be.domain.job.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CrawledJobDto {
    private String title;                 // 공고 제목
    private String detailUrl;             // 원본 공고 링크 URL
    private String reward;                // 채용 보상금 등 (필요 시)
    private String locationDescription;   // 수집된 원본 지역 텍스트 (예: "서울 강남구")

    private CrawledCompanyDto company;    // 내포된 기업 정보 DTO

    private List<String> categories;      // 수집된 직무 카테고리 태그 목록
    private List<String> skills;          // 수집된 기술 스택 태그 목록

    //
    private String intro;                 // 회사 및 서비스 소개 (TEXT)
    private String mainTasks;             // 담당 업무 / 주요 업무 (TEXT)
    private String requirements;          // 자격 요건 / 지원 자격 (TEXT)
    private String preferredPoints;       // 우대 사항 (TEXT)
    private String benefits;              // 복지 / 혜택 / 회사 문화 (TEXT)
    private Integer careerMin;            // 최소 경력 연수
    private Integer careerMax;            // 최대 경력 연수
    private String categoryName;          // 직무 카테고리 이름
    private String regionName;            // 근무 지역 이름
}
