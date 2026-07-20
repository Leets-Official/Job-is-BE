package com.leets7th.job_is_be.domain.job.service;

import com.leets7th.job_is_be.domain.job.dto.CrawledCompanyDto;
import com.leets7th.job_is_be.domain.job.entity.Company;
import com.leets7th.job_is_be.domain.job.repository.CompanyRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Builder
@RequiredArgsConstructor
public class CompanyService {
    private final CompanyRepository companyRepository;

    @Transactional
    public Company getOrCreateCompany(CrawledCompanyDto companyDto) {
        if (companyDto == null || companyDto.getName() == null) return null;

        return companyRepository.findByName(companyDto.getName())
                .orElseGet(() -> {
                    // [이곳에 추가] 저장 직전에 로그를 출력하여 어떤 값이 null인지 확인
                    log.info("저장할 회사 정보: name={}, source={}, logo={}",
                            companyDto.getName(), "wanted", companyDto.getLogoUrl());

                    return companyRepository.saveAndFlush(
                            Company.builder()
                                    .name(companyDto.getName())
                                    .logoUrl(companyDto.getLogoUrl())
                                    .description(companyDto.getDescription())
                                    .source("wanted")
                                    .build()
                    );
                });
    }
}
