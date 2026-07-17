package com.leets7th.job_is_be.domain.job.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JobPostingSyncService {

    private final JobSyncPageProcessor pageProcessor;

    @EventListener(ApplicationReadyEvent.class)
    public void syncOnStartup() {
        sync();
    }

    // job_postings에는 동기화 여부를 나타내는 플래그가 없어, 매 실행마다 전체를 훑어
    // (source, externalId) 기준으로 upsert한다
    @Scheduled(cron = "0 0 * * * *")
    public void sync() {
        int page = 0;
        while (pageProcessor.processPage(page++)) {
        }
    }
}
