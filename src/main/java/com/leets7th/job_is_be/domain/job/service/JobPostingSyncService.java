package com.leets7th.job_is_be.domain.job.service;

import com.leets7th.job_is_be.domain.job.converter.JobPostingConverter;
import com.leets7th.job_is_be.domain.job.entity.JobPosting;
import com.leets7th.job_is_be.domain.job.repository.JobPostingRepository;
import com.leets7th.job_is_be.domain.job.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class JobPostingSyncService {

    private final JobPostingRepository jobPostingRepository;
    private final JobRepository jobRepository;
    private final JobPostingConverter converter;

    // job_postings에는 동기화 여부를 나타내는 플래그가 없어, 매 실행마다 전체를 훑어
    // (source, externalId) 기준으로 upsert한다
    @Scheduled(cron = "0 54 * * * *")
    public void sync() {
        for (JobPosting posting : jobPostingRepository.findAll()) {
            jobRepository.findBySourceAndExternalId(posting.getSource(), posting.getExternalId())
                    .ifPresentOrElse(
                            existing -> converter.updateFrom(existing, posting),
                            () -> jobRepository.save(converter.createFrom(posting))
                    );
        }
    }
}
