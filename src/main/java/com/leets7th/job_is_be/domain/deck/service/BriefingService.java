package com.leets7th.job_is_be.domain.deck.service;

import com.leets7th.job_is_be.domain.deck.dto.BriefingResponse;
import com.leets7th.job_is_be.domain.deck.dto.CardResponse;
import com.leets7th.job_is_be.domain.deck.entity.Card;
import com.leets7th.job_is_be.domain.deck.entity.Deck;
import com.leets7th.job_is_be.domain.deck.repository.CardRepository;
import com.leets7th.job_is_be.domain.deck.repository.DeckRepository;
import com.leets7th.job_is_be.domain.job.entity.JobCategory;
import com.leets7th.job_is_be.domain.job.enums.JobStatus;
import com.leets7th.job_is_be.domain.job.repository.JobRepository;
import com.leets7th.job_is_be.domain.user.repository.UserRepository;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BriefingService {

    private static final String MORNING_SLOT = "07:30";
    private static final String LUNCH_SLOT = "12:30";
    private static final String EVENING_SLOT = "18:30";

    private static final LocalTime LUNCH_CUTOFF = LocalTime.of(10, 0);
    private static final LocalTime EVENING_CUTOFF = LocalTime.of(15, 30);

    private static final String DEFAULT_THEME = "오늘의 맞춤 공고를 준비했습니다.";

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final DeckRepository deckRepository;
    private final CardRepository cardRepository;
    private final CardService cardService;

    // REC-01 안내 문구 출력
    public BriefingResponse getTodayBriefing(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new GeneralException(ErrorStatus.USER_NOT_FOUND);
        }



        OffsetDateTime now = OffsetDateTime.now();
        LocalDate today = now.toLocalDate();
        String slot = resolveSlot(now.toLocalTime());
        String greeting = resolveGreeting(slot);

        // 지원가능 건수 — 마감되지 않은 전체 공고 수 조회
        long applicableCount = jobRepository.countApplicable(JobStatus.ACTIVE, now);

        // 오늘의 Deck 조회
        Optional<Deck> deck = deckRepository.findByUserIdAndDeckDate(userId, today);
        List<Card> cards = deck.map(d -> cardRepository.findByDeckId(d.getId()))
                .orElse(List.of());

        return new BriefingResponse(
                greeting,
                applicableCount,
                cards.size(),
                resolveTheme(cards)
        );
    }

    // REC-02 브리핑 덱 — 오늘 덱 카드 목록 조회
    public List<CardResponse> getTodayBriefingStatus(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new GeneralException(ErrorStatus.USER_NOT_FOUND);
        }

        Deck deck = deckRepository.findByUserIdAndDeckDate(userId, OffsetDateTime.now().toLocalDate())
                .orElseThrow(() -> new GeneralException(ErrorStatus.DECK_NOT_FOUND));

        return cardService.getDeckCards(deck.getId(), userId);
    }

    // 시간대 별 인사문구 추출
    private String resolveSlot(LocalTime now) {
        if (now.isBefore(LUNCH_CUTOFF)) {
            return MORNING_SLOT;
        }
        if (now.isBefore(EVENING_CUTOFF)) {
            return LUNCH_SLOT;
        }
        return EVENING_SLOT;
    }

    // 시간대 별 인사문구
    private String resolveGreeting(String slot) {
        return switch (slot) {
            case MORNING_SLOT -> "⛅ 좋은 아침입니다";
            case LUNCH_SLOT -> "\uD83C\uDF1E 즐거운 점심 시간입니다";
            default -> "\uD83C\uDF19 좋은 저녁입니다";
        };
    }

    // 테마 + 로더 — 큐레이션 테마 한 줄과 로딩 문구 추출
    private String resolveTheme(List<Card> cards) {
        Map<String, Long> categoryCounts = cards.stream()
                .filter(card -> card.getJob() != null && card.getJob().getJobCategory() != null)
                .map(card -> card.getJob().getJobCategory())
                .collect(Collectors.groupingBy(JobCategory::getName, Collectors.counting()));

        return categoryCounts.entrySet().stream()
                .max(Comparator.comparingLong(Map.Entry::getValue))
                .map(entry -> "오늘은 " + entry.getKey() + " 직무 위주로 골랐습니다.")
                .orElse(DEFAULT_THEME);
    }
}
