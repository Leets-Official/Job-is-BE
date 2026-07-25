package com.leets7th.job_is_be.domain.job.repository;

import com.leets7th.job_is_be.domain.job.dto.JobSearchRequest;
import com.leets7th.job_is_be.domain.job.dto.JobSummaryResponse;
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
        List<JobSummaryResponse> content = queryFactory
                .select(Projections.constructor(JobSummaryResponse.class,
                        job.id,
                        job.company.name,
                        job.title,
                        job.careerLevel,
                        job.employmentType,
                        job.remoteAvailable,
                        job.deadlineAt,
                        Expressions.nullExpression(String.class),
                        job.skillTags
                ))
                .from(job)
                .leftJoin(job.company)
                .where(
                        keywordContains(request.keyword()),
                        regionsIn(request.regions()),
                        skillTagsIn(request.skillTags()),
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
        return skillTags.stream()
                .map(tag -> (BooleanExpression) Expressions.booleanTemplate("{0} = ANY({1})", tag.getValue(), job.skillTags))
                .reduce(BooleanExpression::or)
                .orElse(null);
    }

    private BooleanExpression categoryChildEq(String categoryChild) {
        return StringUtils.hasText(categoryChild) ? job.jobCategory.name.eq(categoryChild) : null;
    }
}
