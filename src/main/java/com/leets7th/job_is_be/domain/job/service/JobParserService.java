package com.leets7th.job_is_be.domain.job.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leets7th.job_is_be.domain.job.dto.CrawledJobDto;
import com.leets7th.job_is_be.domain.job.dto.CrawledCompanyDto;
import com.leets7th.job_is_be.domain.job.entity.Company;
import com.leets7th.job_is_be.domain.job.entity.Job;
import com.leets7th.job_is_be.domain.job.entity.JobCategory;
import com.leets7th.job_is_be.domain.job.entity.Region;
import com.leets7th.job_is_be.domain.job.enums.JobStatus;
import com.leets7th.job_is_be.domain.job.repository.JobCategoryRepository;
import com.leets7th.job_is_be.domain.job.repository.JobRepository;
import com.leets7th.job_is_be.domain.job.repository.RegionRepository;
import com.leets7th.job_is_be.global.config.CrawlerProperties;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

@Slf4j
@Service
@Transactional
public class JobParserService {

    private final CrawlerProperties crawlerProperties;
    private final CompanyService companyService;
    private final JobCategoryRepository jobCategoryRepository;
    private final RegionRepository regionRepository;
    private final JobRepository jobRepository;
    private final ObjectMapper objectMapper;

    public JobParserService(CrawlerProperties crawlerProperties, CompanyService companyService,
                            JobCategoryRepository jobCategoryRepository, RegionRepository regionRepository,
                            JobRepository jobRepository, ObjectMapper objectMapper) {
        this.crawlerProperties = crawlerProperties;
        this.companyService = companyService;
        this.jobCategoryRepository = jobCategoryRepository;
        this.regionRepository = regionRepository;
        this.jobRepository = jobRepository;
        this.objectMapper = objectMapper;
    }

    public void parseAndSave() {
        log.info("크롤링 결과 파싱 및 DB 적재 시작...");

        processFile(crawlerProperties.getCompanyOutputPath(), "회사", (line) -> {
            CrawledCompanyDto companyDto = objectMapper.readValue(line, CrawledCompanyDto.class);
            companyService.getOrCreateCompany(companyDto);
        });

        processFile(crawlerProperties.getJobOutputPath(), "공고", (line) -> {
            CrawledJobDto jobDto = objectMapper.readValue(line, CrawledJobDto.class);

            //title이 null이거나 빈 값인 경우 스킵
            if (jobDto.title() == null || jobDto.title().isBlank()) {
                log.warn("공고 제목(title)이 누락되어 저장을 건너뜁니다. (externalId: {})", jobDto.externalId());
                return;
            }

            Company company = companyService.getOrCreateCompany(jobDto.company());
            JobCategory jobCategory = jobCategoryRepository.findByName(jobDto.categoryName()).orElse(null);
            Region region = regionRepository.findByName(jobDto.regionName()).orElse(null);

            java.util.Optional<Job> existingJob = jobRepository.findBySourceAndExternalId(jobDto.source(), jobDto.externalId());

            Job job;
            if (existingJob.isPresent()) {
                job = existingJob.get();
                job.syncFrom(
                        company,
                        jobDto.title(),
                        jobDto.careerLevel(),
                        jobDto.employmentType(),
                        jobDto.remoteAvailable(),
                        jobDto.detailUrl(),
                        jobDto.postedAt(),
                        jobDto.deadlineAt(),
                        JobStatus.ACTIVE,
                        null,
                        jobDto.mainTasks(),
                        jobDto.requirements(),
                        jobDto.preferredPoints(),
                        jobDto.skills(),
                        null,
                        null  // embedding은 별도 배치로 계산
                );
            } else {
                job = Job.builder()
                        .title(jobDto.title())
                        .sourceUrl(jobDto.detailUrl())
                        .rewardTotal(jobDto.reward())
                        .company(company)
                        .intro(jobDto.intro())
                        .mainTasks(jobDto.mainTasks())
                        .requirements(jobDto.requirements())
                        .preferredPoints(jobDto.preferredPoints())
                        .benefits(jobDto.benefits())
                        .careerMin(jobDto.careerMin())
                        .careerMax(jobDto.careerMax())
                        .jobCategory(jobCategory)
                        .region(region)
                        .source(jobDto.source())
                        .externalId(jobDto.externalId())
                        .skills(jobDto.skills() != null ? String.join(",", jobDto.skills()) : null)
                        .categories(jobDto.categories() != null ? String.join(",", jobDto.categories()) : null)
                        .build();
            }

            jobRepository.save(job);
        });

        log.info("모든 파이프라인 데이터 적재 완료");
    }

    private void processFile(String path, String type, CheckedConsumer processor) {
        if (path == null) {
            log.error("{} 경로가 설정되지 않았습니다 (null).", type);
            return;
        }
        File file = new File(path);
        if (!file.exists()) {
            log.error("{} 파일이 존재하지 않습니다: {}", type, path);
            return;
        }

        int lineNumber = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;

                // 빈 줄 제외
                if (line.trim().isEmpty()) {
                    continue;
                }

                processor.accept(line);
            }
        } catch (Exception e) {
            log.error("{} 처리 중 오류 발생 [라인 번호: {}] - 원인: {}", type, lineNumber, e.getMessage(), e);
            throw new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @FunctionalInterface
    interface CheckedConsumer { void accept(String line) throws Exception; }
}
