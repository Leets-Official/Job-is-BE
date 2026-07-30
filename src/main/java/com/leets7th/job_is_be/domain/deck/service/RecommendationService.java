package com.leets7th.job_is_be.domain.deck.service;

import com.leets7th.job_is_be.domain.deck.dto.CardResponse;
import com.leets7th.job_is_be.domain.deck.entity.Card;
import com.leets7th.job_is_be.domain.deck.entity.Deck;
import com.leets7th.job_is_be.domain.deck.enums.DeckState;
import com.leets7th.job_is_be.domain.deck.repository.CardRepository;
import com.leets7th.job_is_be.domain.deck.repository.DeckRepository;
import com.leets7th.job_is_be.domain.job.dto.SimilarJobItemDto;
import com.leets7th.job_is_be.domain.job.entity.Job;
import com.leets7th.job_is_be.domain.job.repository.JobRepository;
import com.leets7th.job_is_be.domain.job.service.JobSimilarService;
import com.leets7th.job_is_be.domain.user.entity.User;
import com.leets7th.job_is_be.domain.user.repository.UserRepository;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 추천 파이프라인 — 오늘의 Deck을 만들고 Card로 채운다.
 * 후보 선별·적합도 산출은 {@link JobSimilarService}(pgvector 코사인 유사도 기반 파이썬 엔진)에 위임한다.
 * 카드 요약(summary)은 화면설계서 REC §0.3에 따라 JD 필드를 조합한 규칙 기반 템플릿(≤90자)으로 생성한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RecommendationService {

    // 하루 덱 크기 — 화면설계서 REC-01 ③ 기준 3~6건
    private static final int DECK_SIZE = 5;
    private static final int SUMMARY_MAX_LENGTH = 90;
    private static final int SUMMARY_SKILL_LIMIT = 3;

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final DeckRepository deckRepository;
    private final CardRepository cardRepository;
    private final CardService cardService;
    private final JobSimilarService jobSimilarService;

    // 오늘 덱이 없으면 추천엔진을 돌려 생성하고, 이미 있으면 그대로 반환
    public List<CardResponse> generateTodayDeck(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        LocalDate today = OffsetDateTime.now().toLocalDate();
        Deck deck = deckRepository.findByUserIdAndDeckDate(userId, today)
                .orElseGet(() -> deckRepository.save(Deck.builder().user(user).deckDate(today).build()));

        if (cardRepository.findByDeckId(deck.getId()).isEmpty()) {
            fillFromRecommendationEngine(deck, userId);
        }

        return cardService.getDeckCards(deck.getId(), userId);
    }

    /**
     * 추천엔진 결과로 카드를 채운다.
     * 후보가 없거나 성향 퀴즈가 없으면 카드를 만들지 않고 빈 상태 원인만 기록한다(REC-07).
     */
    private void fillFromRecommendationEngine(Deck deck, Long userId) {
        List<SimilarJobItemDto> items;
        try {
            items = jobSimilarService.getRecommendedJobsByPersonality(userId).items();
        } catch (GeneralException e) {
            if (ErrorStatus.PERSONALITY_NOT_FOUND.equals(e.getErrorStatus())) {
                // 성향 퀴즈 미완료 = 첫 레터를 만들 근거가 없음
                deck.markEmpty(DeckState.ONBOARDING_INCOMPLETE.getCode());
                return;
            }
            throw e;
        }

        if (items == null || items.isEmpty()) {
            deck.markEmpty(DeckState.NO_CANDIDATES.getCode());
            return;
        }

        Map<Long, Job> jobsByExternalId = findJobsByExternalId(items);

        List<Card> cards = new ArrayList<>();
        for (SimilarJobItemDto item : items) {
            if (cards.size() >= DECK_SIZE) {
                break;
            }
            Job job = resolveJob(item, jobsByExternalId);
            if (job == null) {
                continue;
            }
            cards.add(Card.builder()
                    .deck(deck)
                    .job(job)
                    .position(cards.size() + 1)
                    .fitScore(BigDecimal.valueOf(item.fitScore()))
                    .reason(resolveReason(item))
                    .summary(buildSummary(job))
                    .build());
        }

        if (cards.isEmpty()) {
            // 엔진은 후보를 냈지만 서빙용 Job으로 되돌리지 못한 경우(원문 미동기화 등)
            log.warn("[Deck] 추천 후보 {}건을 Job으로 매칭하지 못했습니다. userId={}", items.size(), userId);
            deck.markEmpty(DeckState.NO_CANDIDATES.getCode());
            return;
        }
        cardRepository.saveAll(cards);
    }

    /**
     * 엔진이 돌려준 식별자는 원문 external_id 다. 한 번에 조회해 N+1을 피한다.
     */
    private Map<Long, Job> findJobsByExternalId(List<SimilarJobItemDto> items) {
        List<Long> externalIds = items.stream()
                .map(item -> parseId(item.jobId()))
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();

        if (externalIds.isEmpty()) {
            return Map.of();
        }
        return jobRepository.findByExternalIdIn(externalIds).stream()
                .collect(Collectors.toMap(Job::getExternalId, Function.identity(), (a, b) -> a, LinkedHashMap::new));
    }

    private Job resolveJob(SimilarJobItemDto item, Map<Long, Job> jobsByExternalId) {
        Long id = parseId(item.jobId());
        if (id == null) {
            return null;
        }
        Job job = jobsByExternalId.get(id);
        // 엔진이 external_id 대신 내부 PK를 돌려준 경우에 대한 보정
        return job != null ? job : jobRepository.findById(id).orElse(null);
    }

    private Long parseId(String value) {
        try {
            return value == null ? null : Long.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 추천 이유(REC-03 ③). 엔진 문장이 비면 근거 배열로 대체하고, 그것도 없으면 null(줄 생략).
     */
    private String resolveReason(SimilarJobItemDto item) {
        if (item.reason() != null && !item.reason().isBlank()) {
            return item.reason();
        }
        if (item.fitPoints() != null && !item.fitPoints().isEmpty()) {
            return String.join(" · ", item.fitPoints());
        }
        return null;
    }

    /**
     * 한눈에 요약(REC-03 ⑧) — 지역·고용형태·경력·스킬·마감 5개 축을 조합한 규칙 기반 템플릿.
     * 생성할 재료가 하나도 없으면 null 을 반환해 미리보기 블록을 축소시킨다.
     */
    private String buildSummary(Job job) {
        List<String> parts = new ArrayList<>();

        String location = job.getLocationCity() != null ? job.getLocationCity() : job.getLocationFull();
        if (location != null && !location.isBlank()) {
            parts.add(location);
        }
        if (job.getEmploymentType() != null && !job.getEmploymentType().isBlank()) {
            parts.add(job.getEmploymentType());
        }
        if (job.getCareerLevel() != null && !job.getCareerLevel().isBlank()) {
            parts.add(job.getCareerLevel());
        }
        if (job.getSkillTags() != null && !job.getSkillTags().isEmpty()) {
            parts.add(String.join(", ", job.getSkillTags().stream().limit(SUMMARY_SKILL_LIMIT).toList()));
        }
        parts.add(resolveDeadlineLabel(job.getDeadlineAt()));

        if (parts.isEmpty()) {
            return null;
        }

        String summary = String.join(" · ", parts);
        return summary.length() <= SUMMARY_MAX_LENGTH ? summary : summary.substring(0, SUMMARY_MAX_LENGTH - 1) + "…";
    }

    // 마감일 없으면 "상시", 있으면 D-day (REC-03 ⑦ 마감 배지 규칙)
    private String resolveDeadlineLabel(OffsetDateTime deadlineAt) {
        if (deadlineAt == null) {
            return "상시";
        }
        long days = ChronoUnit.DAYS.between(OffsetDateTime.now().toLocalDate(), deadlineAt.toLocalDate());
        return days <= 0 ? "D-DAY" : "D-" + days;
    }
}
