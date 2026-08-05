package com.leets7th.job_is_be.domain.job.repository;

import com.leets7th.job_is_be.domain.job.dto.JobFitSignalsDto;
import com.leets7th.job_is_be.domain.job.dto.JobSearchRequest;
import com.leets7th.job_is_be.domain.job.dto.JobSummaryResponse;
import com.leets7th.job_is_be.domain.job.entity.Job;
import com.leets7th.job_is_be.domain.job.entity.JobCategory;
import com.leets7th.job_is_be.domain.job.enums.JobSortType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 검색 필터의 cities 파라미터가 실제로 job.locationCity를 걸러내는지 검증한다
 * (SearchJobsParams.cities <-> JobSearchRequest.cities 이름 불일치로 인해
 * 지금까지 프론트에서 지역 필터가 조용히 무시되고 있었음).
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class JobRepositoryCustomImplTest {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobCategoryRepository jobCategoryRepository;

    @Test
    void cities_필터로_해당_지역_공고만_조회된다() {
        jobRepository.save(activeJob("서울 공고", "wanted", 1L, "서울"));
        jobRepository.save(activeJob("부산 공고", "wanted", 2L, "부산"));

        JobSearchRequest request = JobSearchRequest.builder()
                .cities(List.of("서울"))
                .build();

        Page<JobSummaryResponse> result = jobRepository.searchJobs(request, PageRequest.of(0, 10), null, Map.of());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).position()).isEqualTo("서울 공고");
    }

    /**
     * 추천순(FIT) 정렬 시, 유저 희망 직무와 일치하는 공고가 안 맞는 공고보다 위로 오는지 검증한다.
     * (JobRepositoryCustomImpl.fitScore — 직무 일치 +3점)
     */
    @Test
    void 추천순_정렬시_희망직무가_일치하는_공고가_위로_온다() {
        JobCategory matchingCategory = jobCategoryRepository.save(
                JobCategory.builder().name("백엔드 개발자").groupName("백엔드").sortOrder(1).build());
        JobCategory otherCategory = jobCategoryRepository.save(
                JobCategory.builder().name("프론트엔드 개발자").groupName("프론트엔드").sortOrder(2).build());

        jobRepository.save(activeJobWithCategory("안 맞는 공고", "wanted", 10L, otherCategory));
        jobRepository.save(activeJobWithCategory("맞는 공고", "wanted", 11L, matchingCategory));

        JobFitSignalsDto signals = new JobFitSignalsDto(
                List.of(matchingCategory.getId()), null, false, null, null);
        JobSearchRequest request = JobSearchRequest.builder().sort(JobSortType.FIT).build();

        Page<JobSummaryResponse> result = jobRepository.searchJobs(request, PageRequest.of(0, 10), signals, Map.of());

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).position()).isEqualTo("맞는 공고");
    }

    private Job activeJob(String title, String source, Long externalId, String locationCity) {
        Job job = Job.builder()
                .title(title)
                .source(source)
                .externalId(externalId)
                .locationCity(locationCity)
                .build();
        return job;
    }

    private Job activeJobWithCategory(String title, String source, Long externalId, JobCategory jobCategory) {
        Job job = Job.builder()
                .title(title)
                .source(source)
                .externalId(externalId)
                .jobCategory(jobCategory)
                .build();
        return job;
    }
}
