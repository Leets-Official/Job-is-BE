package com.leets7th.job_is_be.domain.job.repository;

import com.leets7th.job_is_be.domain.job.dto.JobSearchRequest;
import com.leets7th.job_is_be.domain.job.dto.JobSummaryResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.leets7th.job_is_be.domain.job.entity.QJob.job;
import static com.leets7th.job_is_be.domain.company.entity.QCompany.company; // 회사 엔티티 경로에 맞게 조절

@RequiredArgsConstructor
public class JobRepositoryCustomImpl implements JobRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<JobSummaryResponse> searchJobs(JobSearchRequest request, Pageable pageable) {
        List<JobSummaryResponse> content = queryFactory
                .select(Projections.constructor(JobSummaryResponse.class,
                        job.id,
                        company.name,
                        job.position,
                        job.careerLevel,
                        job.employmentType,
                        job.remoteAvailable,
                        job.deadlineAt, // 기존 엔티티 필드명(dueTime 또는 deadlineAt)에 맞춤
                        job.thumbnailUrl,
                        job.skillTags
                ))
                .from(job)
                .leftJoin(job.company, company)
                .where(
                        keywordEq(request.keyword()),
                        categoryChildEq(request.categoryChild()),
                        skillTagEq(request.skillTag()),
                        regionEq(request.region())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(job.count())
                .from(job)
                .leftJoin(job.company, company)
                .where(
                        keywordEq(request.keyword()),
                        categoryChildEq(request.categoryChild()),
                        skillTagEq(request.skillTag()),
                        regionEq(request.region())
                )
                .fetchOne();

        long totalCount = total != null ? total : 0L;
        return new PageImpl<>(content, pageable, totalCount);
    }

    private BooleanExpression keywordEq(String keyword) {
        return StringUtils.hasText(keyword) ? job.position.containsIgnoreCase(keyword)
                .or(company.name.containsIgnoreCase(keyword)) : null;
    }

    private BooleanExpression categoryChildEq(String categoryChild) {
        return StringUtils.hasText(categoryChild) ? job.categoryChild.eq(categoryChild) : null;
    }

    private BooleanExpression skillTagEq(String skillTag) {
        return StringUtils.hasText(skillTag) ? job.skillTags.contains(skillTag) : null;
    }

    private BooleanExpression regionEq(String region) {
        return StringUtils.hasText(region) ? job.region.containsIgnoreCase(region) : null;
    }
}
