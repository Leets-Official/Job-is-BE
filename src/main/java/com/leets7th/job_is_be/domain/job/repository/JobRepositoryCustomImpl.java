package com.leets7th.job_is_be.domain.job.repository;

import com.leets7th.job_is_be.domain.job.dto.JobSearchRequest;
import com.leets7th.job_is_be.domain.job.dto.JobSummaryResponse;
import com.leets7th.job_is_be.domain.job.entity.Job;
import com.leets7th.job_is_be.domain.job.enums.TechStackType;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.leets7th.job_is_be.domain.job.entity.QJob.job;

@Repository
@RequiredArgsConstructor
public class JobRepositoryCustomImpl implements JobRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<JobSummaryResponse> searchJobs(JobSearchRequest request, Pageable pageable) {
        // Job 엔티티를 조회 (페치 조인 활용)
        List<Job> jobs = queryFactory
                .selectFrom(job)
                .leftJoin(job.company).fetchJoin()
                .where(
                        keywordContains(request.keyword()),
                        regionsIn(request.regions()),
                        skillTagsIn(request.skillTags()),
                        categoryChildEq(request.categoryChild())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 조회된 Job 엔티티를 JobSummaryResponse로 변환하며 스킬 태그(TechStackResponse) 매핑
        List<JobSummaryResponse> content = jobs.stream()
                .map(JobSummaryResponse::from)
                .toList();

        Long total = queryFactory
                .select(job.count())
                .from(job)
                .where(
                        keywordContains(request.keyword()),
                        regionsIn(request.regions()),
                        skillTagsIn(request.skillTags()),
                        categoryChildEq(request.categoryChild())
                )
                .fetchOne();

        long totalCount = total != null ? total : 0L;
        return new PageImpl<>(content, pageable, totalCount);
    }



    private BooleanExpression keywordContains(String keyword) {
        return StringUtils.hasText(keyword) ? job.title.containsIgnoreCase(keyword).or(job.company.name.containsIgnoreCase(keyword)) : null;
    }

    private BooleanExpression regionsIn(List<String> regions) {
        if (regions == null || regions.isEmpty()) {
            return null;
        }
        return regions.stream()
                .map(job.region.name::containsIgnoreCase)
                .reduce(BooleanExpression::or)
                .orElse(null);
    }

    private BooleanExpression skillTagsIn(List<TechStackType> skillTags) {
        if (skillTags == null || skillTags.isEmpty()) {
            return null;
        }
        // TechStackType을 DB에 저장된 형식)으로 변환
        List<String> tagValues = skillTags.stream()
                .map(TechStackType::getValue)
                .toList();

        return job.skillTags.any().in(tagValues);
    }

    private BooleanExpression categoryChildEq(String categoryChild) {
        return StringUtils.hasText(categoryChild) ? job.jobCategory.name.eq(categoryChild) : null;
    }
}
