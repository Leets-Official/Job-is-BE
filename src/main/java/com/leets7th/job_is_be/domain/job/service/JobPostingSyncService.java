package com.leets7th.job_is_be.domain.job.service;

import com.leets7th.job_is_be.domain.job.converter.JobPostingConverter;
import com.leets7th.job_is_be.domain.job.entity.JobPosting;
import com.leets7th.job_is_be.domain.job.repository.JobPostingRepository;
import com.leets7th.job_is_be.domain.job.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class JobPostingSyncService {

    private static final int PAGE_SIZE = 200;

    private final JobPostingRepository jobPostingRepository;
    private final JobRepository jobRepository;
    private final JobPostingConverter converter;

    // 앱 실행시 자동으로 Jobposting DB에 있는 값들을 가져온다
    @EventListener(ApplicationReadyEvent.class)
    public void syncOnStartup() {
        sync();
    }

    // job_postings에는 동기화 여부를 나타내는 플래그가 없어, 매 실행마다 전체를 훑어
    // (source, externalId) 기준으로 upsert한다
    @Scheduled(cron = "0 0 * * * *")
    public void sync() {
        int page = 0;
        Slice<JobPosting> chunk;
        do {
            chunk = jobPostingRepository.findAll(PageRequest.of(page++, PAGE_SIZE));
            for (JobPosting posting : chunk) {
                jobRepository.findBySourceAndExternalId(posting.getSource(), posting.getExternalId())
                        .ifPresentOrElse(
                                existing -> converter.updateFrom(existing, posting),
                                () -> jobRepository.save(converter.createFrom(posting))
                        );
            }
        } while (chunk.hasNext());
    }
}
