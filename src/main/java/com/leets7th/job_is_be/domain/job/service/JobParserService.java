package com.leets7th.job_is_be.domain.job.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leets7th.job_is_be.domain.job.dto.CrawledJobDto;
import com.leets7th.job_is_be.domain.job.dto.CrawledCompanyDto;
import com.leets7th.job_is_be.domain.job.entity.Company;
import com.leets7th.job_is_be.domain.job.entity.Job;
import com.leets7th.job_is_be.domain.job.entity.JobCategory;
import com.leets7th.job_is_be.domain.job.entity.Region;
import com.leets7th.job_is_be.domain.job.repository.JobCategoryRepository;
import com.leets7th.job_is_be.domain.job.repository.JobRepository;
import com.leets7th.job_is_be.domain.job.repository.RegionRepository;
import com.leets7th.job_is_be.global.config.CrawlerProperties;
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

            Company company = companyService.getOrCreateCompany(jobDto.getCompany());
            JobCategory jobCategory = jobCategoryRepository.findByName(jobDto.getCategoryName()).orElse(null);
            Region region = regionRepository.findByName(jobDto.getRegionName()).orElse(null);

            Job job = Job.builder()
                    .title(jobDto.getTitle())
                    .sourceUrl(jobDto.getDetailUrl())
                    .rewardTotal(jobDto.getReward())
                    .company(company)
                    .intro(jobDto.getIntro())
                    .mainTasks(jobDto.getMainTasks())
                    .requirements(jobDto.getRequirements())
                    .preferredPoints(jobDto.getPreferredPoints())
                    .benefits(jobDto.getBenefits())
                    .careerMin(jobDto.getCareerMin())
                    .careerMax(jobDto.getCareerMax())
                    .jobCategory(jobCategory)
                    .region(region)
                    .build();

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
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                processor.accept(line);
            }
        } catch (Exception e) {
            log.error("{} 처리 중 오류 발생: ", type, e);
        }
    }

    @FunctionalInterface
    interface CheckedConsumer { void accept(String line) throws Exception; }
}
