package com.leets7th.job_is_be.domain.job.service;

import com.leets7th.job_is_be.domain.job.dto.CrawledCompanyDto;
import com.leets7th.job_is_be.domain.job.entity.Company;
import com.leets7th.job_is_be.domain.job.repository.CompanyRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Builder
@RequiredArgsConstructor
public class CompanyService {
    private final CompanyRepository companyRepository;

    @Transactional
    public Company getOrCreateCompany(CrawledCompanyDto companyDto) {
        if (companyDto == null || companyDto.name() == null) return null;

        return companyRepository.findByName(companyDto.name())
                .orElseGet(() -> {
                    log.info("저장할 회사 정보: name={}, source={}, logo={}",
                            companyDto.name(), "wanted", companyDto.logoUrl());

                    return companyRepository.saveAndFlush(
                            Company.builder()
                                    .name(companyDto.name())
                                    .normalizedName(companyDto.normalizedName())
                                    .logoUrl(companyDto.logoUrl())
                                    .description(companyDto.description())
                                    .registrationNumber(companyDto.registrationNumber())
                                    .wantedCompanyId(companyDto.wantedCompanyId())
                                    .jobkoreaGnoRef(companyDto.jobkoreaGnoRef())
                                    .employeeCount(companyDto.employeeCount())
                                    .companyType(companyDto.companyType())
                                    .industry(companyDto.industry())
                                    .stockStatus(companyDto.stockStatus())
                                    .hqAddress(companyDto.hqAddress())
                                    .enrichmentStatus(companyDto.enrichmentStatus())
                                    .nameMatch(companyDto.nameMatch())
                                    .rejectedName(companyDto.rejectedName())
                                    .rawJobkorea(companyDto.rawJobkorea())
                                    .source("wanted")
                                    .build()
                    );
                });
    }
}
