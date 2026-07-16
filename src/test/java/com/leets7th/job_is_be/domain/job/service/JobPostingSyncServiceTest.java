package com.leets7th.job_is_be.domain.job.service;

import com.leets7th.job_is_be.domain.job.converter.JobPostingConverter;
import com.leets7th.job_is_be.domain.job.entity.Company;
import com.leets7th.job_is_be.domain.job.entity.Job;
import com.leets7th.job_is_be.domain.job.entity.JobPosting;
import com.leets7th.job_is_be.domain.job.repository.JobPostingRepository;
import com.leets7th.job_is_be.domain.job.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobPostingSyncServiceTest {

    @Mock
    private JobPostingRepository jobPostingRepository;
    @Mock
    private JobRepository jobRepository;

    private JobPostingSyncService syncService;

    @BeforeEach
    void setUp() {
        syncService = new JobPostingSyncService(jobPostingRepository, jobRepository, new JobPostingConverter());
    }

    @Test
    void 매칭되는_Job이_없으면_새로_생성해서_저장한다() {
        Company company = Company.builder().name("래브라도랩스").build();
        JobPosting posting = JobPosting.builder()
                .source("wanted").externalId(123L).sourceUrl("https://wanted.example/123")
                .company(company).position("백엔드 엔지니어").employmentType("정규직")
                .isRemote(false).status("active")
                .build();

        when(jobPostingRepository.findAll()).thenReturn(List.of(posting));
        when(jobRepository.findBySourceAndExternalId("wanted", 123L)).thenReturn(Optional.empty());

        syncService.sync();

        ArgumentCaptor<Job> captor = ArgumentCaptor.forClass(Job.class);
        verify(jobRepository).save(captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo("백엔드 엔지니어");
        assertThat(captor.getValue().getExternalId()).isEqualTo(123L);
        assertThat(captor.getValue().getCompany()).isEqualTo(company);
    }

    @Test
    void 매칭되는_Job이_있으면_저장_없이_기존_엔티티를_갱신한다() {
        Job existing = Job.builder().source("wanted").externalId(123L).title("old title").build();
        JobPosting posting = JobPosting.builder()
                .source("wanted").externalId(123L).position("new title").employmentType("정규직")
                .isRemote(true).status("active")
                .build();

        when(jobPostingRepository.findAll()).thenReturn(List.of(posting));
        when(jobRepository.findBySourceAndExternalId("wanted", 123L)).thenReturn(Optional.of(existing));

        syncService.sync();

        verify(jobRepository, never()).save(org.mockito.ArgumentMatchers.any());
        assertThat(existing.getTitle()).isEqualTo("new title");
        assertThat(existing.isRemoteAvailable()).isTrue();
    }
}
