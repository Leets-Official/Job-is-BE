package com.leets7th.job_is_be.domain.job.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.leets7th.job_is_be.domain.job.entity.Company;
import com.leets7th.job_is_be.domain.job.entity.Job;
import com.leets7th.job_is_be.domain.job.repository.JobCategoryRepository;
import com.leets7th.job_is_be.domain.job.repository.JobRepository;
import com.leets7th.job_is_be.domain.job.repository.RegionRepository;
import com.leets7th.job_is_be.global.config.CrawlerProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobParserServiceTest {

    @Mock
    private CompanyService companyService;
    @Mock
    private JobCategoryRepository jobCategoryRepository;
    @Mock
    private RegionRepository regionRepository;
    @Mock
    private JobRepository jobRepository;

    @TempDir
    Path tempDir;

    private JobParserService jobParserService;
    private CrawlerProperties crawlerProperties;

    @BeforeEach
    void setUp() {
        crawlerProperties = new CrawlerProperties();
        jobParserService = new JobParserService(
                crawlerProperties, companyService, jobCategoryRepository,
                regionRepository, jobRepository, new ObjectMapper().registerModule(new JavaTimeModule())
        );
    }

    @Test
    void 신규_공고_저장_시_경력_고용형태_재택여부_게시일_마감일_스킬태그가_채워진다() throws IOException {
        Path companyFile = tempDir.resolve("companies.jsonl");
        Files.writeString(companyFile, "");

        Path jobFile = tempDir.resolve("jobs.jsonl");
        String jsonLine = "{\"position\":\"백엔드 엔지니어\",\"source_url\":\"https://example.com/job/1\","
                + "\"skill_tags\":[\"Java\",\"Spring\"],\"careerLevel\":\"신입\",\"employmentType\":\"정규직\","
                + "\"remoteAvailable\":true,\"postedAt\":\"2026-08-01T00:00:00+09:00\","
                + "\"deadlineAt\":\"2026-09-01T00:00:00+09:00\",\"careerMin\":0,\"careerMax\":1,"
                + "\"source\":\"wanted\",\"externalId\":123}";
        Files.writeString(jobFile, jsonLine + System.lineSeparator());

        crawlerProperties.setCompanyOutputPath(companyFile.toString());
        crawlerProperties.setJobOutputPath(jobFile.toString());

        when(companyService.getOrCreateCompany(any())).thenReturn(testCompany());
        when(jobRepository.findBySourceAndExternalId("wanted", 123L)).thenReturn(Optional.empty());

        jobParserService.parseAndSave();

        ArgumentCaptor<Job> captor = ArgumentCaptor.forClass(Job.class);
        verify(jobRepository).save(captor.capture());
        Job saved = captor.getValue();

        assertThat(saved.getCareerLevel()).isEqualTo("신입");
        assertThat(saved.getEmploymentType()).isEqualTo("정규직");
        assertThat(saved.getRemoteAvailable()).isTrue();
        assertThat(saved.getPostedAt()).isNotNull();
        assertThat(saved.getDeadlineAt()).isNotNull();
        assertThat(saved.getSkillTags()).containsExactly("Java", "Spring");
    }

    private Company testCompany() {
        return Company.builder().name("테스트 회사").build();
    }
}
