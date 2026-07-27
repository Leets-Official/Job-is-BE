package com.leets7th.job_is_be.domain.job.repository;

import com.leets7th.job_is_be.domain.job.dto.JobSearchRequest;
import com.leets7th.job_is_be.domain.job.dto.JobSummaryResponse;
import com.leets7th.job_is_be.domain.job.entity.Job;
import com.leets7th.job_is_be.domain.job.entity.QCompany;
import com.leets7th.job_is_be.domain.job.entity.QJobCategory;
import com.leets7th.job_is_be.domain.job.entity.QRegion;
import com.leets7th.job_is_be.domain.job.enums.TechStackType;
import com.querydsl.core.BooleanBuilder;
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
        QCompany company = QCompany.company;
        QRegion region = QRegion.region;
        QJobCategory jobCategory = QJobCategory.jobCategory;

        // 공고 목록 조회 (LEFT JOIN & FETCH JOIN으로 N+1 방지 및 NULL 허용)
        List<Job> jobs = queryFactory
                .selectFrom(job)
                .leftJoin(job.company, company).fetchJoin()
                .leftJoin(job.region, region).fetchJoin()
                .leftJoin(job.jobCategory, jobCategory).fetchJoin()
                .where(
                        keywordContains(company, request.keyword()),
                        regionsIn(region, request.regions()),
                        skillTagsIn(request.skillTags()),
                        categoryChildEq(jobCategory, request.categoryChild())
                )
                .orderBy(job.createdAt.desc(), job.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        List<JobSummaryResponse> content = jobs.stream()
                .map(JobSummaryResponse::from)
                .toList();

        // 전체 카운트 조회 (목록 조회와 동일한 LEFT JOIN 적용)
        Long total = queryFactory
                .select(job.count())
                .from(job)
                .leftJoin(job.company, company)
                .leftJoin(job.region, region)
                .leftJoin(job.jobCategory, jobCategory)
                .where(
                        keywordContains(company, request.keyword()),
                        regionsIn(region, request.regions()),
                        skillTagsIn(request.skillTags()),
                        categoryChildEq(jobCategory, request.categoryChild())
                )
                .fetchOne();

        long totalCount = total != null ? total : 0L;
        return new PageImpl<>(content, pageable, totalCount);
    }



    private BooleanExpression keywordContains(QCompany company, String keyword) {
        return StringUtils.hasText(keyword)
                ? job.title.containsIgnoreCase(keyword).or(company.name.containsIgnoreCase(keyword))
                : null;
    }

    private BooleanExpression regionsIn(QRegion region, List<String> regions) {
        if (regions == null || regions.isEmpty()) {
            return null;
        }
        return regions.stream()
                .map(region.name::containsIgnoreCase)
                .reduce(BooleanExpression::or)
                .orElse(null);
    }

    private BooleanExpression skillTagsIn(List<TechStackType> skillTags) {
        if (skillTags == null || skillTags.isEmpty()) {
            return null;
        }

        return skillTags.stream()
                .map(TechStackType::getValue)
                .map(tag -> (BooleanExpression) Expressions.booleanTemplate("array_contains({0}, {1})", job.skillTags, tag))
                .reduce(BooleanExpression::or)
                .orElse(null);
    }

    private BooleanExpression categoryChildEq(QJobCategory jobCategory, String categoryChild) {
        return StringUtils.hasText(categoryChild) ? jobCategory.name.eq(categoryChild) : null;
    }
}
