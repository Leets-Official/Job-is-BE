package com.leets7th.job_is_be.global.init;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.leets7th.job_is_be.domain.job.entity.Company;
import com.leets7th.job_is_be.domain.job.entity.Job;
import com.leets7th.job_is_be.domain.job.repository.CompanyRepository;
import com.leets7th.job_is_be.domain.job.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

@Component
@RequiredArgsConstructor
public class JobDataInitializer implements CommandLineRunner {

    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);

        loadCompanies("database/out/companies.jsonl");
        loadJobs("database/out/job_postings.jsonl");
    }

    private void loadCompanies(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                Company company = objectMapper.readValue(line, Company.class);

                // 이미 존재하는 회사가 아니면 저장
                if (company.getId() != null && companyRepository.existsById(company.getId())) {
                    continue;
                }
                companyRepository.save(company);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadJobs(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                Job job = objectMapper.readValue(line, Job.class);

                if (job.getTitle() == null) {
                    continue;
                }

                if (!jobRepository.existsBySourceAndExternalId(job.getSource(), job.getExternalId())) {
                    jobRepository.save(job);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
