package com.leets7th.job_is_be.domain.job.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JobPostingSyncService {

    private final JobSyncPageProcessor pageProcessor;

    public void syncOnStartup() {
        sync();
    }

    // 자동 트리거(@Scheduled, @EventListener) 제거함: JobPosting은 크롤러가 값을 채우지 않아
    // 항상 비어 있고, 이 상태로 동기화가 돌면 Job의 정상 데이터를 null로 덮어쓰거나
    // title(NOT NULL) 제약 위반으로 실패한다. JobParserService가 Job에 직접 저장하는
    // 현재 구조에서는 이 동기화가 필요 없다.
    // job_postings에는 동기화 여부를 나타내는 플래그가 없어, 매 실행마다 전체를 훑어
    // (source, externalId) 기준으로 upsert한다
    public void sync() {
        int page = 0;
        while (pageProcessor.processPage(page++)) {
        }
    }
}
