package com.leets7th.job_is_be.domain.job.repository;

import com.leets7th.job_is_be.domain.job.dto.JobSearchRequest;
import com.leets7th.job_is_be.domain.job.dto.JobSummaryResponse;
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
        List<JobSummaryResponse> content = queryFactory
                .select(Projections.constructor(JobSummaryResponse.class,
                        job.id,
                        job.company.name,
                        job.title,
                        job.careerLevel,
                        job.employmentType,
                        job.remoteAvailable,
                        job.deadlineAt,
                        Expressions.asString((String) null),
                        job.skillTags
                ))
                .from(job)
                .leftJoin(job.company)
                .where(
                        keywordContains(request.keyword()),
                        regionEq(request.region()),
                        skillTagEq(request.skillTag()),
                        categoryChildEq(request.categoryChild())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(job.count())
                .from(job)
                .where(
                        keywordContains(request.keyword()),
                        regionEq(request.region()),
                        skillTagEq(request.skillTag()),
                        categoryChildEq(request.categoryChild())
                )
                .fetchOne();

        long totalCount = total != null ? total : 0L;
        return new PageImpl<>(content, pageable, totalCount);
    }

    private BooleanExpression keywordContains(String keyword) {
        return StringUtils.hasText(keyword) ? job.title.containsIgnoreCase(keyword).or(job.company.name.containsIgnoreCase(keyword)) : null;
    }

    private BooleanExpression regionEq(String region) {
        return StringUtils.hasText(region) ? job.locationFull.containsIgnoreCase(region) : null;
    }

    private BooleanExpression skillTagEq(String skillTag) {
        return StringUtils.hasText(skillTag) ? job.skillTags.contains(skillTag) : null;
    }

    private BooleanExpression categoryChildEq(String categoryChild) {
        return StringUtils.hasText(categoryChild) ? job.jobCategory.name.eq(categoryChild) : null;
    }
}
