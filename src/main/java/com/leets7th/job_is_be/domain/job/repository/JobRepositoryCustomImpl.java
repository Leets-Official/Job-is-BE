package com.leets7th.job_is_be.domain.job.repository;

import com.leets7th.job_is_be.domain.job.dto.JobSearchRequest;
import com.leets7th.job_is_be.domain.job.dto.JobSummaryResponse;
import com.leets7th.job_is_be.domain.job.entity.Job;
import com.leets7th.job_is_be.domain.job.entity.QCompany;
import com.leets7th.job_is_be.domain.job.entity.QJobCategory;
import com.leets7th.job_is_be.domain.job.entity.QRegion;
import com.leets7th.job_is_be.domain.job.enums.CareerRange;
import com.leets7th.job_is_be.domain.job.enums.JobStatus;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
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

        OffsetDateTime now = OffsetDateTime.now();

        BooleanExpression[] conditions = new BooleanExpression[]{
                candidateUniverse(now),
                alwaysOpenIncluded(request),
                keywordContains(company, request.keyword()),
                categoryChildrenIn(request.categoryChildren()),
                regionsIn(request.regions()),
                districtsIn(request.districts()),
                careerRangesIn(request.careerRanges()),
                employmentTypesIn(request.employmentTypes()),
                remoteOnly(request)
        };

        // 공고 목록 조회 (LEFT JOIN & FETCH JOIN으로 N+1 방지 및 NULL 허용)
        List<Job> jobs = queryFactory
                .selectFrom(job)
                .leftJoin(job.company, company).fetchJoin()
                .leftJoin(job.region, region).fetchJoin()
                .leftJoin(job.jobCategory, jobCategory).fetchJoin()
                .where(conditions)
                .orderBy(orderBy(request))
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
                .where(conditions)
                .fetchOne();

        long totalCount = total != null ? total : 0L;
        return new PageImpl<>(content, pageable, totalCount);
    }

    /**
     * 후보 유니버스(화면설계서 EXP §2.1) — ACTIVE 이면서 미마감 또는 상시채용인 공고만.
     */
    private BooleanExpression candidateUniverse(OffsetDateTime now) {
        return job.status.eq(JobStatus.ACTIVE)
                .and(job.deadlineAt.isNull().or(job.deadlineAt.gt(now)));
    }

    /**
     * "상시채용 포함" off → 마감일이 있는 공고만 남긴다(§4.2).
     */
    private BooleanExpression alwaysOpenIncluded(JobSearchRequest request) {
        return request.includeAlwaysOpenOrDefault() ? null : job.deadlineAt.isNotNull();
    }

    /**
     * 키워드 — 공고명 · 회사명 · 스킬 태그 부분 매칭(§3.2 ②).
     */
    private BooleanExpression keywordContains(QCompany company, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        BooleanExpression skillTagMatches = Expressions.booleanTemplate(
                "array_to_string({0}, ',') ilike {1}", job.skillTags, "%" + keyword + "%");

        return job.title.containsIgnoreCase(keyword)
                .or(company.name.containsIgnoreCase(keyword))
                .or(skillTagMatches);
    }

    /**
     * 세부직군 — category_child 배열에 선택값이 하나라도 포함되면 통과(§4.2).
     */
    private BooleanExpression categoryChildrenIn(List<String> categoryChildren) {
        if (CollectionUtils.isEmpty(categoryChildren)) {
            return null;
        }
        // PostgreSQL 에는 array_contains 가 없다. 배열에 값이 있으면 첨자를, 없으면 NULL 을 돌려주는
        // array_position 으로 판정한다(= ANY 는 HQL 이 부질의 한정자로 해석해 파싱이 깨진다).
        return categoryChildren.stream()
                .filter(StringUtils::hasText)
                .map(value -> (BooleanExpression) Expressions.booleanTemplate(
                        "array_position({0}, {1}) is not null", job.categoryChild, value))
                .reduce(BooleanExpression::or)
                .orElse(null);
    }

    /** 지역 시·도 필터 — 공고의 locationCity 와 대조한다(§4.2). */
    private BooleanExpression regionsIn(List<String> regions) {
        List<String> values = nonBlank(regions);
        return values.isEmpty() ? null : job.locationCity.in(values);
    }

    private BooleanExpression districtsIn(List<String> districts) {
        List<String> values = nonBlank(districts);
        return values.isEmpty() ? null : job.locationDistrict.in(values);
    }

    /**
     * 빈 문자열 항목을 걸러낸다.
     *
     * <p>{@code ?regions=} 처럼 값 없이 넘어온 항목을 그대로 두면 {@code IN ('')} 이 만들어져
     * 일치하는 공고가 없어지고, 필터를 안 건 것처럼 보이는 대신 결과가 0건이 된다.
     * 빈 항목만 들어온 경우는 필터를 걸지 않은 것으로 본다(categoryChildrenIn 과 동일한 정책).
     */
    private List<String> nonBlank(List<String> values) {
        if (CollectionUtils.isEmpty(values)) {
            return List.of();
        }
        return values.stream().filter(StringUtils::hasText).toList();
    }

    /**
     * 경력 구간 — 선택 구간들의 OR(§4.2).
     */
    private BooleanExpression careerRangesIn(List<CareerRange> careerRanges) {
        if (CollectionUtils.isEmpty(careerRanges)) {
            return null;
        }
        return careerRanges.stream()
                .map(this::careerRangeCondition)
                .reduce(BooleanExpression::or)
                .orElse(null);
    }

    private BooleanExpression careerRangeCondition(CareerRange range) {
        return switch (range) {
            case NEWCOMER -> job.isNewbie.isTrue().or(job.careerMin.eq(0));
            case JUNIOR -> job.careerMin.loe(3).and(job.careerMax.goe(1));
            case SENIOR -> job.careerMax.goe(4);
        };
    }

    private BooleanExpression employmentTypesIn(List<String> employmentTypes) {
        List<String> values = nonBlank(employmentTypes);
        return values.isEmpty() ? null : job.employmentType.in(values);
    }

    private BooleanExpression remoteOnly(JobSearchRequest request) {
        return request.remoteOnlyOrFalse() ? job.remoteAvailable.isTrue() : null;
    }

    /**
     * 정렬(§3.4). 상시채용(마감일 NULL)은 마감임박순에서 항상 뒤로 보낸다.
     * 추천순은 적합도 파이프라인이 붙기 전까지 "산출 불가 → 최신순 tie-break" 규칙으로 동작한다.
     */
    private OrderSpecifier<?>[] orderBy(JobSearchRequest request) {
        OrderSpecifier<?> latest = job.postedAt.desc().nullsLast();

        return switch (request.sortOrDefault()) {
            case DEADLINE -> new OrderSpecifier<?>[]{
                    job.deadlineAt.asc().nullsLast(), latest, job.id.desc()
            };
            case FIT, RECENT -> new OrderSpecifier<?>[]{latest, job.id.desc()};
        };
    }
}
